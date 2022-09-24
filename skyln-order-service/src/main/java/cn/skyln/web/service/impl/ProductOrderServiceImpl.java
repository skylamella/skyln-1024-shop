package cn.skyln.web.service.impl;

import cn.skyln.enums.*;
import cn.skyln.exception.BizException;
import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.LoginUser;
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
import cn.skyln.web.model.VO.CouponRecordVO;
import cn.skyln.web.model.VO.OrderItemVO;
import cn.skyln.web.model.VO.ProductOrderAddressVO;
import cn.skyln.web.service.ProductOrderService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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
        // 发送延迟消息，用于自动关单 todo
        // 创建支付 todo
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, addressVO);
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
            return itemDO;
        }).collect(Collectors.toList());
        int rows = orderItemMapper.insertBatch(list);
        if(rows != list.size()){
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
        // todo 过滤支付方式
        ProductOrderPayTypeEnum.valueOf(confirmOrderRequest.getPayType());
        productOrderDO.setPayType(confirmOrderRequest.getPayType());
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
}
