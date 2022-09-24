package cn.skyln.web.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: lamella
 * @Date: 2022/09/19/22:08
 * @Description:
 */
@Data
@ApiModel(value = "锁定优惠券请求对象", description = "锁定优惠券请求对象")
public class LockCouponRecordDTO {

    @ApiModelProperty(value = "优惠券记录ID列表", example = "[1,2]")
    @JsonProperty("lock_coupon_record_ids")
    private List<Long> lockCouponRecordIds;

    @ApiModelProperty(value = "订单号", example = "123456789")
    @JsonProperty("order_out_trade_no")
    private String orderOutTradeNo;
}
