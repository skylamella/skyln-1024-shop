package cn.skyln.user.web.model.REQ;

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
    @ApiModelProperty(value = "密码")
    private String pwd;

    @ApiModelProperty(value = "邮箱")
    private String mail;

    private String ip;

    @ApiModelProperty(value = "安全模式")
    private int safeMode;
}
