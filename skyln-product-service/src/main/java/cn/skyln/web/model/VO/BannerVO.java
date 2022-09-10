package cn.skyln.web.model.VO;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author skylamella
 * @since 2022-09-10
 */
@Data
public class BannerVO {

    private Integer id;

    /**
     * 图片
     */
    private String img;

    /**
     * 跳转地址
     */
    private String url;

    /**
     * 权重
     */
    private Integer weight;
}
