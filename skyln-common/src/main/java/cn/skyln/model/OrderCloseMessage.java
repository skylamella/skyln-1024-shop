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
     * 订单号
     */
    private String outTradeNo;
}
