package cn.skyln.web.model.REQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/12/21:18
 * @Description:
 */
@Data
@ApiModel(value = "重新支付订单对象", description = "重新支付订单对象")
public class RepayOrderRequest {

    /**
     * 支付方式
     */
    @JsonProperty("pay_type")
    private String payType;

    /**
     * 客户端类型
     */
    @JsonProperty("client_type")
    private String clientType;

    /**
     * 防重令牌
     */
    private String token;

    /**
     * 订单号
     */
    @JsonProperty("out_trade_no")
    private String orderOutTradeNo;

    /**
     * 支付商品标题
     */
    @JsonProperty("pay_title")
    private String payTitle;

}
