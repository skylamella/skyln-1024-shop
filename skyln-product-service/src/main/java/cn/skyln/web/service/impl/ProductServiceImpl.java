package cn.skyln.web.service.impl;

import cn.skyln.config.RabbitMQConfig;
import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.ProductOrderStateEnum;
import cn.skyln.enums.StockTaskStateEnum;
import cn.skyln.exception.BizException;
import cn.skyln.model.ProductStockMessage;
import cn.skyln.utils.CommonUtils;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.ProductOrderFeignService;
import cn.skyln.web.mapper.ProductMapper;
import cn.skyln.web.mapper.ProductTaskMapper;
import cn.skyln.web.model.DO.ProductDO;
import cn.skyln.web.model.DO.ProductTaskDO;
import cn.skyln.web.model.DTO.LockProductDTO;
import cn.skyln.web.model.DTO.OrderItemDTO;
import cn.skyln.web.model.VO.ProductDetailVO;
import cn.skyln.web.model.VO.ProductListVO;
import cn.skyln.web.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-10
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductTaskMapper productTaskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private ProductOrderFeignService productOrderFeignService;

    /**
     * 分页查询商品
     *
     * @param page 第几页
     * @param size 一页显示几条
     * @return Map
     */
    @Override
    public Map<String, Object> pageProductActivity(int page, int size) {
        Page<ProductDO> pageInfo = new Page<>(page, size);
        IPage<ProductDO> productDOIPage = productMapper.selectPage(pageInfo, null);
        return CommonUtils.getReturnPageMap(productDOIPage.getTotal(),
                productDOIPage.getPages(),
                productDOIPage.getRecords().stream().map(obj ->
                                CommonUtils.beanProcess(obj, new ProductListVO()))
                        .collect(Collectors.toList()));
    }

    /**
     * 根据ID查询商品详情
     *
     * @param productId 商品ID
     * @return ProductDetailVO
     */
    @Override
    public ProductDetailVO findDetailById(long productId) {
        ProductDO productDO = productMapper.selectById(productId);
        return beanProcess(productDO);
    }

    /**
     * 根据ID批量查询商品
     *
     * @param productIdList ID列表
     * @return 商品列表
     */
    @Override
    public List<ProductDetailVO> findProductsByIdBatch(List<Long> productIdList) {
        List<ProductDO> productDOList = productMapper.selectList(new QueryWrapper<ProductDO>().in("id", productIdList));
        return productDOList.stream().map(this::beanProcess).collect(Collectors.toList());
    }

    /**
     * 锁定商品库存
     * 1）遍历商品，锁定每个商品购买数量
     * 2）每一次锁定的时候，都要发送延迟消息
     *
     * @param lockProductDTO 商品锁定对象
     * @return JsonData
     */
    @Override
    public JsonData lockProductStock(LockProductDTO lockProductDTO) {
        String orderOutTradeNo = lockProductDTO.getOrderOutTradeNo();
        List<OrderItemDTO> orderItemList = lockProductDTO.getOrderItemList();
        // 一行代码提取对象里面的ID并加入到集合里面
        List<Long> productIdList = orderItemList.stream().map(OrderItemDTO::getProductId).collect(Collectors.toList());
        // 批量查询
        List<ProductDetailVO> productDetailVOList = this.findProductsByIdBatch(productIdList);
        // 根据ID分组
        Map<Long, ProductDetailVO> productDetailVOMap = productDetailVOList.stream().collect(Collectors.toMap(ProductDetailVO::getId, Function.identity()));
        for (OrderItemDTO item : orderItemList) {
            // 锁定商品记录
            int rows = productMapper.lockProductStock(item.getProductId(), item.getBuyNum());
            if (rows != 1) {
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
            } else {
                // 插入商品product_task
                ProductDetailVO productDetailVO = productDetailVOMap.get(item.getProductId());
                ProductTaskDO productTaskDO = new ProductTaskDO();
                productTaskDO.setBuyNum(item.getBuyNum());
                productTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
                productTaskDO.setProductId(item.getProductId());
                productTaskDO.setProductName(productDetailVO.getTitle());
                productTaskDO.setOutTradeNo(orderOutTradeNo);
                productTaskDO.setCreateTime(new Date());
                productTaskMapper.insert(productTaskDO);
                // 发送MQ延迟消息
                ProductStockMessage productStockMessage = new ProductStockMessage();
                productStockMessage.setOutTradeNo(orderOutTradeNo);
                productStockMessage.setTaskId(productTaskDO.getId());
                rabbitTemplate.convertAndSend(rabbitMQConfig.getStockEventExchange(),
                        rabbitMQConfig.getStockReleaseDelayRoutingKey(),
                        productStockMessage);
                log.info("商品库存锁定延迟消息发送成功：{}", productStockMessage);
            }
        }
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    /**
     * 释放商品库存
     *
     * @param productStockMessage MQ消息体
     * @return 清理结果
     */
    @Override
    public boolean releaseProductStockRecord(ProductStockMessage productStockMessage) {
        // 查询task工作单是否存在
        ProductTaskDO productTaskDO = productTaskMapper.selectById(productStockMessage.getTaskId());
        if (Objects.isNull(productTaskDO)) {
            log.warn("工作单不存在，消息：{}", productStockMessage);
            return true;
        }
        if (StringUtils.equalsIgnoreCase(productTaskDO.getLockState(), StockTaskStateEnum.LOCK.name())) {
            // 查询订单状态
            JsonData jsonData = productOrderFeignService.queryProductOrderState(productStockMessage.getOutTradeNo());
            if (jsonData.getCode() == 0) {
                // 正常响应，判断订单状态
                String state = jsonData.getData().toString();
                // 返回的数据不存在，返回给消息队列，重新投递
                if (StringUtils.isBlank(state)) {
                    log.warn("订单状态不存在，返回给消息队列，重新投递：{}", productStockMessage);
                    return false;
                }
                // 状态是NEW新建状态，返回给消息队列，重新投递
                if (StringUtils.equalsIgnoreCase(ProductOrderStateEnum.NEW.name(), state)) {
                    log.warn("订单状态是NEW，返回给消息队列，重新投递：{}", productStockMessage);
                    return false;
                }
                // 状态是已经支付
                if (StringUtils.equalsIgnoreCase(ProductOrderStateEnum.PAY.name(), state)) {
                    // 修改task状态为finish
                    productTaskDO.setLockState(StockTaskStateEnum.FINISH.name());
                    int rows = productTaskMapper.updateById(productTaskDO);
                    if (rows > 0) {
                        log.info("订单已经支付，修改库存锁定工作单状态为FINISH：{}", productStockMessage);
                        return true;
                    } else {
                        log.warn("订单已经支付，修改库存锁定工作单状态为FINISH失败，返回给消息队列，重新投递：{}", productStockMessage);
                        return false;
                    }
                }
            }
            // 订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复商品库存
            log.warn("订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复商品库存：{}", productStockMessage);
            productTaskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            productTaskMapper.updateById(productTaskDO);
            // 恢复商品库存
            productMapper.unlockProductStock(productTaskDO.getProductId(), productTaskDO.getBuyNum());
        } else {
            log.warn("工作单状态不是LOCK，state={}，消息：{}", productTaskDO.getLockState(), productStockMessage);
        }
        return true;
    }

    private ProductDetailVO beanProcess(ProductDO productDO) {
        ProductDetailVO productDetailVO = new ProductDetailVO();
        BeanUtils.copyProperties(productDO, productDetailVO);
        productDetailVO.setStock(productDO.getStock() - productDO.getLockStock());
        return productDetailVO;
    }
}
