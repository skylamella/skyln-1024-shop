package cn.skyln.web.model.REQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: lamella
 * @Date: 2022/09/21/22:07
 * @Description:
 */
@Data
@ApiModel(value = "商品锁定对象", description = "商品锁定对象")
public class LockProductRequest {

    @ApiModelProperty(value = "订单号", example = "123456789")
    @JsonProperty("order_out_trade_no")
    private String orderOutTradeNo;

    @ApiModelProperty(value = "订单项列表")
    @JsonProperty("order_item_list")
    private List<OrderItemRequest> orderItemList;
}
