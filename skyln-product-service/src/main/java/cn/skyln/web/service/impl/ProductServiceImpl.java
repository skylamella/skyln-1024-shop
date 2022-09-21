package cn.skyln.web.service.impl;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.StockTaskStateEnum;
import cn.skyln.exception.BizException;
import cn.skyln.utils.CommonUtils;
import cn.skyln.utils.JsonData;
import cn.skyln.web.mapper.ProductMapper;
import cn.skyln.web.mapper.ProductTaskMapper;
import cn.skyln.web.model.DO.ProductDO;
import cn.skyln.web.model.DO.ProductTaskDO;
import cn.skyln.web.model.REQ.LockProductRequest;
import cn.skyln.web.model.REQ.OrderItemRequest;
import cn.skyln.web.model.VO.ProductDetailVO;
import cn.skyln.web.model.VO.ProductListVO;
import cn.skyln.web.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductTaskMapper productTaskMapper;

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
     * @param lockProductRequest 商品锁定对象
     * @return JsonData
     */
    @Override
    public JsonData lockProductStock(LockProductRequest lockProductRequest) {
        String orderOutTradeNo = lockProductRequest.getOrderOutTradeNo();
        List<OrderItemRequest> orderItemList = lockProductRequest.getOrderItemList();
        // 一行代码提取对象里面的ID并加入到集合里面
        List<Long> productIdList = orderItemList.stream().map(OrderItemRequest::getProductId).collect(Collectors.toList());
        // 批量查询
        List<ProductDetailVO> productDetailVOList = this.findProductsByIdBatch(productIdList);
        // 根据ID分组
        Map<Long, ProductDetailVO> productDetailVOMap = productDetailVOList.stream().collect(Collectors.toMap(ProductDetailVO::getId, Function.identity()));
        for (OrderItemRequest item : orderItemList) {
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
                // 发送MQ延迟消息 todo
            }
        }
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    private ProductDetailVO beanProcess(ProductDO productDO) {
        ProductDetailVO productDetailVO = new ProductDetailVO();
        BeanUtils.copyProperties(productDO, productDetailVO);
        productDetailVO.setStock(productDO.getStock() - productDO.getLockStock());
        return productDetailVO;
    }
}
