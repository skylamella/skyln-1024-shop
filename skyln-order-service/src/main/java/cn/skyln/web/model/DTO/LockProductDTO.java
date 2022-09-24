package cn.skyln.web.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: lamella
 * @Date: 2022/09/21/22:07
 * @Description:
 */
@Data
public class LockProductDTO {

    @JsonProperty("order_out_trade_no")
    private String orderOutTradeNo;

    @JsonProperty("order_item_list")
    private List<OrderItemDTO> orderItemList;
}
