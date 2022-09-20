package cn.skyln.model;

import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/20/21:13
 * @Description:
 */
@Data
public class CouponRecordMessage {

    /**
     * 消息id
     */
    private Long messageId;
    /**
     * 订单号
     */
    private String outTradeNo;
    /**
     * 库存锁定任务id
     */
    private Long taskId;
}
