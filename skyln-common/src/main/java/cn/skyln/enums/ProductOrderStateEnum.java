package cn.skyln.enums;

/**
 * @Author: lamella
 * @Date: 2022/09/12/21:00
 * @Description:
 */
public enum ProductOrderStateEnum {
    /**
     * 未支付订单
     */
    NEW,

    /**
     * 已经支付订单
     */
    PAY,

    /**
     * 超时取消订单
     */
    CANCEL;
}
