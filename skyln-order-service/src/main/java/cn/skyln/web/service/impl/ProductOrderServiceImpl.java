package cn.skyln.web.service.impl;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.CouponUseStateEnum;
import cn.skyln.exception.BizException;
import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.LoginUser;
import cn.skyln.utils.CommonUtils;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.CouponFeignService;
import cn.skyln.web.feignClient.ProductFeignService;
import cn.skyln.web.feignClient.UserFeignService;
import cn.skyln.web.mapper.ProductOrderMapper;
import cn.skyln.web.model.DO.ProductOrderDO;
import cn.skyln.web.model.DTO.CartDTO;
import cn.skyln.web.model.DTO.CouponDTO;
import cn.skyln.web.model.REQ.ConfirmOrderRequest;
import cn.skyln.web.model.VO.CouponRecordVO;
import cn.skyln.web.model.VO.OrderItemVO;
import cn.skyln.web.model.VO.ProductOrderAddressVO;
import cn.skyln.web.service.ProductOrderService;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, addressVO);
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
     * 判断优惠券是否可用
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
     * @return
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
