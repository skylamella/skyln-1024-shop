package cn.skyln.user.web.model.REQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/04/10:51
 * @Description:
 */
@ApiModel(value = "用户注册对象", description = "用户注册请求对象")
@Data
public class UserRegisterRequest {
    @ApiModelProperty(value = "密码", example = "123456")
    private String pwd;

    @ApiModelProperty(value = "确认密码", example = "123456")
    @JsonProperty("re_pwd")
    private String rePwd;

    @ApiModelProperty(value = "昵称", example = "凉月")
    private String name;

    @ApiModelProperty(value = "头像", example = "凉月")
    @JsonProperty("head_img")
    private String headImg;

    @ApiModelProperty(value = "个人签名", example = "这是一个签名")
    private String slogan;

    @ApiModelProperty(value = "性别，0表示女，1表示男", example = "1")
    private Integer sex;

    @ApiModelProperty(value = "邮箱", example = "lamella@skyln.cn")
    private String mail;

    @ApiModelProperty(value = "验证码", example = "123456")
    private String code;
}
