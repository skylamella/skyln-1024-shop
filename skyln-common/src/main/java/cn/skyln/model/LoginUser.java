package cn.skyln.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/04/18:17
 * @Description:
 */
@Data
public class LoginUser {
    private Long id;

    private String name;

    @JsonProperty("head_img")
    private String headImg;

    private String mail;
}
