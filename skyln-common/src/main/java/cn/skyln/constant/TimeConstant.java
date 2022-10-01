package cn.skyln.constant;

/**
 * @Author: lamella
 * @Date: 2022/10/01/18:39
 * @Description:
 */
public class TimeConstant {

    /**
     * 订单超时，单位毫秒，默认30分钟
     * <p>
     * 支付订单的有效时长，超过未支付则关闭订单
     */
    public static final long ORDER_PAY_TIMEOUT_MILLS = 1000 * 60 * 30;
}
