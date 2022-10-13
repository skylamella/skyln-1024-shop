package cn.skyln.model;

import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/22/21:03
 * @Description:
 */
@Data
public class CartMessage {

    /**
     * 消息队列id
     */
    private Long messageId;
    /**
     * 订单号
     */
    private String outTradeNo;
    /**
     * 商品ID列表
     */
    private Long productId;
    /**
     * 用户ID
     */
    private Long userId;
}
