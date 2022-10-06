package cn.skyln.enums;

import lombok.Getter;

/**
 * @Author: lamella
 * @Date: 2022/10/06/16:11
 * @Description:
 */
public enum MQChannelStateEnum {
    STOCK_RECORD_RELEASE("释放商品库存"),
    MQ_STOCK_RECORD_RELEASE(""),
    CLEAN_CART_RECORD("清空购物车"),
    MQ_CLEAN_CART_RECORD(""),
    COUPON_RECORD_RELEASE("释放优惠券"),
    MQ_COUPON_RECORD_RELEASE(""),
    DELAY_ORDER_CLOSE("延迟自动关单"),
    MQ_DELAY_ORDER_CLOSE("");

    @Getter
    private String msg;

    private MQChannelStateEnum(String msg) {
        this.msg = msg;
    }
}
