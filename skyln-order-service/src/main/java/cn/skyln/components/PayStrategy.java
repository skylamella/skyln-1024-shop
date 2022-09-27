package cn.skyln.components;

import cn.skyln.web.model.VO.PayInfoVO;

/**
 * @Author: lamella
 * @Date: 2022/09/27/22:11
 * @Description:
 */
public interface PayStrategy {

    /**
     * 下单
     * @param payInfoVO
     * @return
     */
    String unifyOrder(PayInfoVO payInfoVO);

    /**
     * 退款
     * @param payInfoVO
     * @return
     */
    default String refund(PayInfoVO payInfoVO){
        return "";
    }

    /**
     * 查询支付是否成功
     * @param payInfoVO
     * @return
     */
    default String queryPaySuccess(PayInfoVO payInfoVO){
        return "";
    }

}
