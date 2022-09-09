package cn.skyln.web.model.REQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/04/10:51
 * @Description:
 */
@ApiModel(value = "用户登录对象", description = "用户登录请求对象")
@Data
public class UserLoginRequest {
    @ApiModelProperty(value = "密码", example = "123456")
    private String pwd;

    @ApiModelProperty(value = "邮箱", example = "lamella@skyln.cn")
    private String mail;

    @ApiModelProperty(value = "安全模式", example = "1")
    @JsonProperty("safe_mode")
    private int safeMode;
}
