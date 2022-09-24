package cn.skyln.web.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/10/20:30
 * @Description:
 */
@Data
@ApiModel(value = "新用户注册领券对象", description = "新用户注册领券对象")
public class NewUserCouponDTO {

    @ApiModelProperty(value = "用户ID", example = "1")
    @JsonProperty("user_id")
    private long userId;

    @ApiModelProperty(value = "用户名称", example = "凉月")
    @JsonProperty("user_name")
    private String userName;
}
