package cn.skyln.web.service.impl;

import cn.skyln.components.factories.PayFactory;
import cn.skyln.config.RabbitMQConfig;
import cn.skyln.constant.TimeConstant;
import cn.skyln.enums.*;
import cn.skyln.exception.BizException;
import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.LoginUser;
import cn.skyln.model.OrderCloseMessage;
import cn.skyln.utils.CommonUtils;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.CouponFeignService;
import cn.skyln.web.feignClient.ProductFeignService;
import cn.skyln.web.feignClient.UserFeignService;
import cn.skyln.web.mapper.ProductOrderItemMapper;
import cn.skyln.web.mapper.ProductOrderMapper;
import cn.skyln.web.model.DO.ProductOrderDO;
import cn.skyln.web.model.DO.ProductOrderItemDO;
import cn.skyln.web.model.DTO.*;
import cn.skyln.web.model.REQ.ConfirmOrderRequest;
import cn.skyln.web.model.REQ.RepayOrderRequest;
import cn.skyln.web.model.VO.*;
import cn.skyln.web.service.ProductOrderService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-12
 */
@Service
@Slf4j
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderService {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private ProductOrderItemMapper orderItemMapper;

    @Autowired
    private UserFeignService userFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private PayFactory payFactory;

    /**
     * 创建订单
     * <p>
     * 1、防重提交
     * 2、用户微服务-确认收货地址
     * 3、商品微服务-获取最新购物项和价格
     * 4、订单验价
     * 4.1、优惠券微服务-获取优惠券
     * 4.2、验证价格
     * 5、锁定优惠券
     * 6、锁定商品库存
     * 7、创建订单对象
     * 8、创建子订单对象
     * 9、发送延迟消息-用于自动关单
     * 10、创建支付信息-对接三方支付
     *
     * @param confirmOrderRequest 确认订单对象
     * @return JsonData
     */
    @Override
    @Transactional
    public JsonData confirmOrder(ConfirmOrderRequest confirmOrderRequest) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String orderOutTradeNo = CommonUtils.getRandomCode(32);
        // 获取用户的收货地址
        ProductOrderAddressVO addressVO = this.getUserAddress(confirmOrderRequest.getAddressId());
        log.info("收货地址信息：{}", addressVO);
        // 获取用户加入购物车的商品
        List<Long> productIdList = confirmOrderRequest.getProductIdList();
        CartDTO cartDTO = new CartDTO();
        cartDTO.setProductIdList(productIdList);
        cartDTO.setOrderOutTradeNo(orderOutTradeNo);
        JsonData jsonData = productFeignService.confirmOrderCartItem(cartDTO);
        if (jsonData.getCode() != 0) {
            log.error("获取用户加入购物车的商品失败，msg：{}", jsonData);
            return JsonData.returnJson(BizCodeEnum.SYSTEM_ERROR);
        }
        List<OrderItemVO> orderItemVOList = jsonData.getData(new TypeReference<>() {
        });
        if (Objects.isNull(orderItemVOList) || orderItemVOList.size() == 0) {
            log.error("购物车商品项不存在，msg：{}", jsonData);
            return JsonData.returnJson(BizCodeEnum.CART_NOT_EXIT);
        }
        log.info("获取到的购物项详情：{}", orderItemVOList);
        // 商品验价
        this.checkAmount(orderItemVOList, confirmOrderRequest, orderOutTradeNo);
        // 锁定优惠券
        this.lockCouponRecords(confirmOrderRequest, orderOutTradeNo);
        // 锁定库存
        this.lockProductStocks(orderItemVOList, orderOutTradeNo);
        // 创建订单
        ProductOrderDO productOrderDO = this.setProductOrder(confirmOrderRequest, loginUser, orderOutTradeNo, addressVO);
        // 创建订单项
        this.setProductOrderItems(orderOutTradeNo, productOrderDO.getId(), orderItemVOList, loginUser);
        // 发送延迟消息，用于自动关单
        OrderCloseMessage orderCloseMessage = new OrderCloseMessage();
        orderCloseMessage.setOutTradeNo(orderOutTradeNo);
        orderCloseMessage.setPayType(confirmOrderRequest.getPayType());
        rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(),
                rabbitMQConfig.getOrderCloseDelayRoutingKey(),
                orderCloseMessage);
        log.info("自动关单延迟消息发送成功：{}", orderCloseMessage);
        // 创建支付
        PayInfoVO payInfoVO = new PayInfoVO();
        payInfoVO.setOutTradeNo(orderOutTradeNo);
        payInfoVO.setPayAmount(confirmOrderRequest.getRealPayAmount());
        payInfoVO.setPayType(confirmOrderRequest.getPayType());
        payInfoVO.setClientType(confirmOrderRequest.getClientType());
        String title = orderItemVOList.get(0).getProductTitle();
        if (orderItemVOList.size() > 1) {
            title = title + "等" + orderItemVOList.size() + "件商品";
        }
        payInfoVO.setTitle(title);
        payInfoVO.setDescription(title);
        // 设置30分钟过期时间
        payInfoVO.setOrderPayTimeMills(TimeConstant.ORDER_PAY_TIMEOUT_MILLS);
        String payResult = payFactory.pay(payInfoVO);
        if (StringUtils.isNotBlank(payResult)) {
            log.info("创建支付订单成功：payInfoVO={}，payResult={}", payInfoVO, payResult);
            return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS, payResult);
        } else {
            log.error("创建支付订单失败：payInfoVO={}", payInfoVO);
            return JsonData.returnJson(BizCodeEnum.PAY_ORDER_FAIL);
        }
    }

    /**
     * 新增订单项
     *
     * @param orderOutTradeNo 订单号
     * @param productOrderId  订单ID
     * @param orderItemVOList 订单项列表
     * @param loginUser       当前登录用户
     */
    private void setProductOrderItems(String orderOutTradeNo, Long productOrderId, List<OrderItemVO> orderItemVOList, LoginUser loginUser) {
        List<ProductOrderItemDO> list = orderItemVOList.stream().map(obj -> {
            ProductOrderItemDO itemDO = new ProductOrderItemDO();
            itemDO.setCreateTime(new Date());

            itemDO.setProductId(obj.getProductId());
            itemDO.setProductImg(obj.getProductImg());
            itemDO.setProductOrderId(productOrderId);
            itemDO.setProductName(obj.getProductTitle());
            itemDO.setOutTradeNo(orderOutTradeNo);

            itemDO.setBuyNum(obj.getBuyNum());
            itemDO.setAmount(obj.getAmount());
            itemDO.setTotalAmount(obj.getTotalAmount());
            log.info("ProductOrderItemDO： {}", itemDO);
            return itemDO;
        }).collect(Collectors.toList());
        int rows = orderItemMapper.insertBatch(list);
        if (rows != list.size()) {
            log.error("新增订单项失败");
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_CART_ITEM_NOT_EXIST);
        }
    }

    /**
     * 创建订单
     *
     * @param confirmOrderRequest 确认订单对象
     * @param loginUser           当前登录用户
     * @param orderOutTradeNo     订单号
     * @param addressVO           收货地址对象
     */
    private ProductOrderDO setProductOrder(ConfirmOrderRequest confirmOrderRequest, LoginUser loginUser, String orderOutTradeNo, ProductOrderAddressVO addressVO) {
        ProductOrderDO productOrderDO = new ProductOrderDO();
        // 设置用户相关信息
        productOrderDO.setUserId(loginUser.getId());
        productOrderDO.setHeadImg(loginUser.getHeadImg());
        productOrderDO.setNickname(loginUser.getName());

        // 设置订单相关信息
        productOrderDO.setOutTradeNo(orderOutTradeNo);
        productOrderDO.setCreateTime(new Date());
        productOrderDO.setDel(0);
        productOrderDO.setOrderType(ProductOrderTypeEnum.DAILY.name());

        // 设置实际支付的价格
        productOrderDO.setPayAmount(confirmOrderRequest.getRealPayAmount());
        // 设置总价，即不使用优惠券的价格
        productOrderDO.setTotalAmount(confirmOrderRequest.getTotalAmount());
        productOrderDO.setState(ProductOrderStateEnum.NEW.name());
        productOrderDO.setPayType(ProductOrderPayTypeEnum.valueOf(confirmOrderRequest.getPayType()).name());

        productOrderDO.setReceiverAddress(JSON.toJSONString(addressVO));
        productOrderMapper.insert(productOrderDO);
        return productOrderDO;
    }

    /**
     * 锁定库存
     *
     * @param orderItemVOList 商品项列表
     * @param orderOutTradeNo 订单号
     */
    private void lockProductStocks(List<OrderItemVO> orderItemVOList, String orderOutTradeNo) {
        LockProductDTO lockProductDTO = new LockProductDTO();
        lockProductDTO.setOrderOutTradeNo(orderOutTradeNo);
        List<OrderItemDTO> orderItemList = orderItemVOList.stream().map(obj -> (OrderItemDTO) CommonUtils.beanProcess(obj, new OrderItemDTO())).collect(Collectors.toList());
        lockProductDTO.setOrderItemList(orderItemList);
        JsonData jsonData = productFeignService.lockProductStocks(lockProductDTO);
        if (jsonData.getCode() != 0) {
            log.error("商品库存锁定失败：{}", jsonData);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
        }
    }

    /**
     * 锁定优惠券
     *
     * @param confirmOrderRequest 确认订单对象
     * @param orderOutTradeNo     订单号
     */
    private void lockCouponRecords(ConfirmOrderRequest confirmOrderRequest, String orderOutTradeNo) {
        log.info("锁定优惠券:{}", confirmOrderRequest);
        List<Long> couponRecordIdList = confirmOrderRequest.getCouponRecordIdList();
        if (Objects.nonNull(couponRecordIdList) && couponRecordIdList.size() > 0) {
            for (Long id : couponRecordIdList) {
                if (id <= 0) {
                    throw new BizException(BizCodeEnum.COUPON_NO_EXITS);
                }
            }
            LockCouponRecordDTO lockCouponRecordDTO = new LockCouponRecordDTO();
            lockCouponRecordDTO.setLockCouponRecordIds(couponRecordIdList);
            lockCouponRecordDTO.setOrderOutTradeNo(orderOutTradeNo);
            JsonData jsonData = couponFeignService.lockCouponRecords(lockCouponRecordDTO);
            log.info("锁定优惠券返回数据：{}", jsonData);
            if (jsonData.getCode() != 0) {
                log.error("优惠券锁定失败：{}", jsonData);
                throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
            }
        }
    }

    /**
     * 验证价格
     * 1）统计全部商品的价格
     * 2）获取优惠券(判断是否满足优惠券的条件)，总价减去优惠券的价格，就是最终的价格
     *
     * @param orderItemVOList     购物项详情
     * @param confirmOrderRequest 确认订单对象
     */
    private void checkAmount(List<OrderItemVO> orderItemVOList, ConfirmOrderRequest confirmOrderRequest, String orderOutTradeNo) {
        // 统计商品总价
        BigDecimal realPayAmount = new BigDecimal(0);
        for (OrderItemVO itemVO : orderItemVOList) {
            realPayAmount = realPayAmount.add(itemVO.getTotalAmount());
        }
        if (realPayAmount.compareTo(confirmOrderRequest.getTotalAmount()) != 0) {
            log.error("订单验价失败：{}", confirmOrderRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
        // 获取使用的优惠券ID列表
        List<Long> couponRecordIdList = confirmOrderRequest.getCouponRecordIdList();
        boolean useCouponFlag = true;
        if (Objects.isNull(couponRecordIdList) || couponRecordIdList.size() == 0) {
            useCouponFlag = false;
        } else if (couponRecordIdList.size() == 1) {
            useCouponFlag = !StringUtils.equals(couponRecordIdList.get(0).toString(), -1 + "");
        } else {
            for (Long l : couponRecordIdList) {
                if (StringUtils.equals(l.toString(), -1 + "")) {
                    useCouponFlag = false;
                    break;
                }
            }
        }
        if (useCouponFlag) {
            // 判断优惠券是否可用
            List<CouponRecordVO> couponRecordVOList = this.getCartCouponRecord(couponRecordIdList, orderOutTradeNo);
            // 计算购物车价格是否满足优惠券满减条件
            if (Objects.isNull(couponRecordVOList) || couponRecordVOList.size() == 0) {
                log.error("优惠券使用失败");
                throw new BizException(BizCodeEnum.COUPON_UNAVAILABLE);
            }
            for (CouponRecordVO couponRecordVO : couponRecordVOList) {
                if (realPayAmount.compareTo(couponRecordVO.getConditionPrice()) < 0) {
                    log.error("不满足优惠券满减金额");
                    throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
                }
                if (couponRecordVO.getPrice().compareTo(realPayAmount) > 0) {
                    realPayAmount = BigDecimal.ZERO;
                } else {
                    realPayAmount = realPayAmount.subtract(couponRecordVO.getPrice());
                }
            }
        }
        if (realPayAmount.compareTo(confirmOrderRequest.getRealPayAmount()) != 0) {
            log.error("订单验价失败：{}", confirmOrderRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
    }

    /**
     * 获取优惠券并判断优惠券是否可用
     *
     * @param couponRecordIdList 优惠券ID列表
     * @return CouponRecordVO列表
     */
    private List<CouponRecordVO> getCartCouponRecord(List<Long> couponRecordIdList, String orderOutTradeNo) {
        if (Objects.isNull(couponRecordIdList) || couponRecordIdList.size() == 0) {
            return null;
        }
        CouponDTO couponDTO = new CouponDTO();
        couponDTO.setCouponRecordIdList(couponRecordIdList);
        couponDTO.setOrderOutTradeNo(orderOutTradeNo);
        JsonData jsonData = couponFeignService.queryUserCouponRecord(couponDTO);
        if (Objects.isNull(jsonData) || jsonData.getCode() != 0) {
            log.error("获取优惠券失败：{}", jsonData);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
        }
        List<CouponRecordVO> couponRecordVOList = jsonData.getData(new TypeReference<>() {
        });
        if (Objects.isNull(couponRecordVOList) || couponRecordVOList.size() == 0) {
            log.error("优惠券使用失败");
            throw new BizException(BizCodeEnum.COUPON_UNAVAILABLE);
        }
        if (!couponAvailable(couponRecordVOList)) {
            log.error("优惠券使用失败");
            throw new BizException(BizCodeEnum.COUPON_CANNOT_USED);
        }
        return couponRecordVOList;
    }

    /**
     * 判断优惠券是否可用
     *
     * @param couponRecordVOList CouponRecordVO列表
     * @return 判断结果
     */
    private boolean couponAvailable(List<CouponRecordVO> couponRecordVOList) {
        boolean flag = true;
        for (CouponRecordVO couponRecordVO : couponRecordVOList) {
            if (StringUtils.equalsIgnoreCase(couponRecordVO.getUseState(), CouponUseStateEnum.NEW.name())) {
                long currentTimeStamp = CommonUtils.getCurrentTimeStamp();
                long end = couponRecordVO.getEndTime().getTime();
                long start = couponRecordVO.getStartTime().getTime();
                if (currentTimeStamp < start || currentTimeStamp > end) {
                    flag = false;
                    break;
                }
            } else {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * 获取收货地址详情
     *
     * @param addressId 收货地址ID
     * @return ProductOrderAddressVO
     */
    private ProductOrderAddressVO getUserAddress(Long addressId) {
        JsonData jsonData = userFeignService.getOneAddress(addressId);
        if (jsonData.getCode() != 0) {
            log.error("RPC-获取收货地址失败");
            throw new BizException(BizCodeEnum.ADDRESS_NOT_EXIT);
        }

        return jsonData.getData(new TypeReference<ProductOrderAddressVO>() {
        });
    }

    /**
     * 查询订单状态
     *
     * @param outTradeNo 订单号
     * @return 订单状态
     */
    @Override
    public String queryProductOrderState(String outTradeNo) {
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>()
                .eq("out_trade_no", outTradeNo));
        return Objects.isNull(productOrderDO) ? null : productOrderDO.getState();
    }

    /**
     * 延迟自动关单
     *
     * @param orderCloseMessage MQ消息体
     * @return 关单结果
     */
    @Override
    public boolean delayCloseProductOrder(OrderCloseMessage orderCloseMessage) {
        String outTradeNo = orderCloseMessage.getOutTradeNo();
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>()
                .eq("out_trade_no", outTradeNo));
        if (Objects.isNull(productOrderDO)) {
            log.info("订单不存在：{}", orderCloseMessage);
            return true;
        }
        // 状态是已经支付
        if (StringUtils.equalsIgnoreCase(ProductOrderStateEnum.PAY.name(), productOrderDO.getState())) {
            // 修改task状态为finish
            log.info("订单已经支付：{}", orderCloseMessage);
            return true;
        }

        // 向第三方支付查询订单是否真的未支付
        PayInfoVO payInfoVO = new PayInfoVO();
        payInfoVO.setOutTradeNo(outTradeNo);
        payInfoVO.setPayType(orderCloseMessage.getPayType());
        String payResult = payFactory.queryPaySuccess(payInfoVO);
        // 结果为空，则未支付成功，本地取消订单
        if (StringUtils.isBlank(payResult)) {
            this.cancelCloseProductOrder(outTradeNo);
            log.info("结果为空，则未支付成功，本地取消订单：{}", orderCloseMessage);
        } else {
            // 支付成功，主动把订单状态改成已支付，造成该情况的原因可能是支付通道回调有问题
            productOrderMapper.updateOrderPayState(outTradeNo, ProductOrderStateEnum.PAY.name(), ProductOrderStateEnum.NEW.name());
            log.warn("支付成功，主动把订单状态改成已支付，造成该情况的原因可能是支付通道回调有问题：{}", orderCloseMessage);
        }
        return true;
    }

    /**
     * 未支付成功，本地取消订单
     *
     * @param outTradeNo 订单号
     */
    @Override
    public void cancelCloseProductOrder(String outTradeNo) {
        productOrderMapper.updateOrderPayState(outTradeNo, ProductOrderStateEnum.CANCEL.name(), ProductOrderStateEnum.NEW.name());
    }

    /**
     * 支付结果回调通知
     *
     * @param payType   支付类型
     * @param paramsMap 回调参数
     * @return JsonData
     */
    @Override
    public JsonData handlerOrderCallbackMsg(ProductOrderPayTypeEnum payType, Map<String, String> paramsMap) {
        if (StringUtils.equalsIgnoreCase(payType.name(), ProductOrderPayTypeEnum.ALIPAY.name())) {
            // 支付宝支付
            // 获取商户订单号
            String outTradeNo = paramsMap.get("out_trade_no");
            // 交易状态
            String tradeStatus = paramsMap.get("trade_status");

            if (StringUtils.equalsIgnoreCase(tradeStatus, "TRADE_SUCCESS") || StringUtils.equalsIgnoreCase(tradeStatus, "TRADE_FINISHED")) {
                // 更新订单状态
                productOrderMapper.updateOrderPayState(outTradeNo, ProductOrderStateEnum.PAY.name(), ProductOrderStateEnum.NEW.name());
                return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
            }
        } else if (StringUtils.equalsIgnoreCase(payType.name(), ProductOrderPayTypeEnum.WECHAT.name())) {
            // todo 微信支付
        }
        return JsonData.returnJson(BizCodeEnum.PAY_ORDER_CALLBACK_NOT_SUCCESS);
    }

    /**
     * 分页查看订单列表
     *
     * @param page      第几页
     * @param size      一页显示几条
     * @param queryType 订单类型
     * @return Map
     */
    @Override
    public Map<String, Object> pageProductActivity(int page, int size, String queryType) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        QueryWrapper<ProductOrderDO> wrapper = new QueryWrapper<ProductOrderDO>().eq("user_id", loginUser.getId());
        if (StringUtils.isNotBlank(queryType) && !StringUtils.equalsIgnoreCase("ALL", queryType)) {
            wrapper.eq("state", queryType);
        }
        Page<ProductOrderDO> pageInfo = new Page<>(page, size);
        IPage<ProductOrderDO> productOrderDOPage = productOrderMapper.selectPage(pageInfo, wrapper);
        return CommonUtils.getReturnPageMap(productOrderDOPage.getTotal(),
                productOrderDOPage.getPages(),
                productOrderDOPage.getRecords().stream().map(obj -> {
                    ProductOrderVO productOrderVO = (ProductOrderVO) CommonUtils.beanProcess(obj, new ProductOrderVO());
                    List<ProductOrderItemDO> list = orderItemMapper.selectList(new QueryWrapper<ProductOrderItemDO>().eq("product_order_id", obj.getId()));
                    List<OrderItemVO> collect = list.stream()
                            .map(orderItemDO -> (OrderItemVO) CommonUtils.beanProcess(orderItemDO, new OrderItemVO()))
                            .collect(Collectors.toList());
                    productOrderVO.setOrderItemVOList(collect);
                    return productOrderVO;
                }).collect(Collectors.toList()));
    }

    /**
     * 重新支付订单
     *
     * @param repayOrderRequest 重新支付订单对象
     * @return JsonData
     */
    @Override
    @Transactional
    public JsonData repayOrder(RepayOrderRequest repayOrderRequest) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String orderOutTradeNo = repayOrderRequest.getOrderOutTradeNo();
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>()
                .eq("user_id", loginUser.getId())
                .eq("out_trade_no", orderOutTradeNo));
        if (Objects.isNull(productOrderDO)) {
            log.error("订单不存在：{}", repayOrderRequest);
            return JsonData.returnJson(BizCodeEnum.PAY_ORDER_NOT_EXIST);
        }
        log.info("订单状态：{}", productOrderDO);
        if (!StringUtils.equalsIgnoreCase(productOrderDO.getState(), ProductOrderStateEnum.NEW.name())) {
            log.error("订单不是NEW状态：{}", repayOrderRequest);
            return JsonData.returnJson(BizCodeEnum.PAY_ORDER_STATE_ERROR);
        }
        long orderLiveTime = CommonUtils.getCurrentTimeStamp() - productOrderDO.getCreateTime().getTime();
        // 创建订单是临界点，所以再增加70秒，假如29分，则也不能支付了
        orderLiveTime = orderLiveTime + 70 * 1000;
        if (orderLiveTime > TimeConstant.ORDER_PAY_TIMEOUT_MILLS) {
            log.error("订单支付超时：{}", repayOrderRequest);
            return JsonData.returnJson(BizCodeEnum.PAY_ORDER_PAY_TIMEOUT);
        }
        if(!StringUtils.equalsIgnoreCase(productOrderDO.getPayType(),repayOrderRequest.getPayType())){
            // 更新订单支付信息
            productOrderDO.setPayType(repayOrderRequest.getPayType());
            productOrderDO.setUpdateTime(new Date());
            productOrderMapper.updateById(productOrderDO);
        }
        // 创建二次支付
        PayInfoVO payInfoVO = new PayInfoVO();
        payInfoVO.setOutTradeNo(orderOutTradeNo);
        payInfoVO.setPayAmount(productOrderDO.getPayAmount());
        payInfoVO.setPayType(repayOrderRequest.getPayType());
        payInfoVO.setClientType(repayOrderRequest.getClientType());
        payInfoVO.setTitle(repayOrderRequest.getPayTitle());
        payInfoVO.setDescription(repayOrderRequest.getPayTitle());
        // 设置过期时间(总时间-存活的时间=剩下的有效时间)
        payInfoVO.setOrderPayTimeMills(TimeConstant.ORDER_PAY_TIMEOUT_MILLS - orderLiveTime);
        log.info("payInfoVO={}", payInfoVO);
        String payResult = payFactory.pay(payInfoVO);
        if (StringUtils.isNotBlank(payResult)) {
            log.info("创建重新支付订单成功：payInfoVO={}，payResult={}", payInfoVO, payResult);
            return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS, payResult);
        } else {
            log.error("创建重新支付订单失败：payInfoVO={}", payInfoVO);
            return JsonData.returnJson(BizCodeEnum.PAY_ORDER_FAIL);
        }
    }
}
