package cn.skyln.components.context;

import cn.skyln.components.PayStrategy;
import cn.skyln.web.model.VO.PayInfoVO;

/**
 * @Author: lamella
 * @Date: 2022/09/27/22:15
 * @Description:
 */
public class PayStrategyContext {

    private PayStrategy payStrategy;

    public PayStrategyContext(PayStrategy payStrategy) {
        this.payStrategy = payStrategy;
    }

    /**
     * 根据支付策略，调用不同的支付接口
     *
     * @param payInfoVO
     * @return
     */
    public String executeUnifyOrder(PayInfoVO payInfoVO) {
        return this.payStrategy.unifyOrder(payInfoVO);
    }

    /**
     * 根据支付策略，调用不同的查询订单支付状态
     *
     * @param payInfoVO
     * @return
     */
    public String executeQueryPaySuccess(PayInfoVO payInfoVO) {
        return this.payStrategy.queryPaySuccess(payInfoVO);
    }
}
