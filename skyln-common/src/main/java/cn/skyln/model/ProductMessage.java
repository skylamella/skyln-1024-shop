package cn.skyln.model;

import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/22/21:03
 * @Description:
 */
@Data
public class ProductMessage {

    /**
     * 消息队列id
     */
    private Long messageId;
    /**
     * 订单号
     */
    private String outTradeNo;
    /**
     * 库存锁定工作单id
     */
    private Long taskId;
}
