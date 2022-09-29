package cn.skyln.model;

import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/24/23:24
 * @Description:
 */
@Data
public class OrderCloseMessage {
    /**
     * 消息队列id
     */
    private Long messageId;

    /**
     * 支付类型 微信-支付宝-银行卡-其他
     */
    private String payType;

    /**
     * 订单号
     */
    private String outTradeNo;
}
