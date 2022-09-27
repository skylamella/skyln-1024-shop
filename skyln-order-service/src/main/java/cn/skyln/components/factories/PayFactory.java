package cn.skyln.components.factories;

import cn.skyln.components.context.PayStrategyContext;
import cn.skyln.components.strategy.impl.AlipayStrategy;
import cn.skyln.components.strategy.impl.WechatPayStrategy;
import cn.skyln.enums.ProductOrderPayTypeEnum;
import cn.skyln.web.model.VO.PayInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author: lamella
 * @Date: 2022/09/27/22:23
 * @Description:
 */
@Component
@Slf4j
public class PayFactory {
    @Autowired
    private AlipayStrategy alipayStrategy;

    @Autowired
    private WechatPayStrategy wechatPayStrategy;

    /**
     * 创建支付，简单工厂模式
     *
     * @param payInfoVO
     * @return
     */
    public String pay(PayInfoVO payInfoVO) {
        PayStrategyContext context = getPayStrategyContext(payInfoVO);
        if (Objects.nonNull(context)) {
            return context.executeUnifyOrder(payInfoVO);
        }
        return "";
    }

    /**
     * 查询订单支付状态
     * 支付成功返回非空，其他返回空
     *
     * @param payInfoVO
     * @return
     */
    public String queryPaySuccess(PayInfoVO payInfoVO) {
        PayStrategyContext context = getPayStrategyContext(payInfoVO);
        if (Objects.nonNull(context)) {
            return context.executeQueryPaySuccess(payInfoVO);
        }
        return "";
    }

    /**
     * 根据PayInfoVO生成对应的上下文
     *
     * @param payInfoVO
     * @return
     */
    private PayStrategyContext getPayStrategyContext(PayInfoVO payInfoVO) {
        String payType = payInfoVO.getPayType();

        if (StringUtils.equalsIgnoreCase(ProductOrderPayTypeEnum.ALIPAY.name(), payType)) {
            // 支付宝支付
            return new PayStrategyContext(alipayStrategy);
        } else if (StringUtils.equalsIgnoreCase(ProductOrderPayTypeEnum.WECHAT.name(), payType)) {
            // 微信支付，暂未实现
            return new PayStrategyContext(wechatPayStrategy);
        }
        return null;
    }
}
