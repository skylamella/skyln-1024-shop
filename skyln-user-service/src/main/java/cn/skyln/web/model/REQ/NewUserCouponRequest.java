package cn.skyln.web.model.REQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/10/20:30
 * @Description:
 */
@Data
@Builder
public class NewUserCouponRequest {

    @JsonProperty("user_id")
    private long userId;

    @JsonProperty("user_name")
    private String userName;
}
