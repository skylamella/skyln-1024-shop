package cn.skyln.web.service.impl;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.exception.BizException;
import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.LoginUser;
import cn.skyln.utils.CommonUtils;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.UserFeignService;
import cn.skyln.web.mapper.ProductOrderMapper;
import cn.skyln.web.model.DO.ProductOrderDO;
import cn.skyln.web.model.REQ.ConfirmOrderRequest;
import cn.skyln.web.model.VO.ProductOrderAddressVO;
import cn.skyln.web.service.ProductOrderService;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        ProductOrderAddressVO addressVO = this.getUserAddress(confirmOrderRequest.getAddressId());
        log.info("收货地址信息：{}",addressVO);
        return null;
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
