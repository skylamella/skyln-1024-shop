package cn.skyln.web.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author: lamella
 * @Date: 2022/09/27/22:06
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayInfoVO {

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 订单金额
     */
    private BigDecimal payAmount;

    /**
     * 支付类型 微信-支付宝-银行卡-其他
     */
    private String payType;

    /**
     * 客户端类型 APP-H5-PC
     */
    private String clientType;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 超时时间，毫秒
     */
    private long orderPayTimeMills;
}
