package cn.skyln.web.model.REQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/21/22:07
 * @Description:
 */
@Data
@ApiModel(value = "商品子项", description = "商品子项")
public class OrderItemRequest {

    @ApiModelProperty(value = "商品ID", example = "1")
    @JsonProperty("product_id")
    private long productId;

    @ApiModelProperty(value = "购买数量", example = "1")
    @JsonProperty("buy_num")
    private int buyNum;
}
