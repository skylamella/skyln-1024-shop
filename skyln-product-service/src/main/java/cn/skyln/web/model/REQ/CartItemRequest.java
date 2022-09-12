package cn.skyln.web.model.REQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/12/15:35
 * @Description:
 */
@ApiModel(value = "加入购物车商品对象", description = "加入购物车商品对象")
@Data
public class CartItemRequest {

    @ApiModelProperty(value = "商品ID", example = "1")
    @JsonProperty("product_id")
    private long productId;

    /**
     * 购买数量
     */
    @ApiModelProperty(value = "购买数量", example = "1")
    @JsonProperty("buy_num")
    private Integer buyNum;

}
