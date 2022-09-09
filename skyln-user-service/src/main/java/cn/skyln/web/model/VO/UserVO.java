package cn.skyln.web.model.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/05/22:08
 * @Description:
 */
@Data
public class UserVO {
    private Long id;

    /**
     * 昵称
     */
    private String name;

    /**
     * 头像
     */
    @JsonProperty("head_img")
    private String headImg;

    /**
     * 用户签名
     */
    private String slogan;

    /**
     * 0表示女，1表示男
     */
    private Integer sex;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 邮箱
     */
    private String mail;
}
