package cn.skyln.web.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author skylamella
 * @since 2022-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("cart_item")
public class CartItemDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 购物车ID
     */
    private Long cartId;

    /**
     * 商品ID
     */
    @JsonProperty("product_id")
    private Long productId;

    /**
     * 购买数量
     */
    @JsonProperty("buy_num")
    private Integer buyNum;

    /**
     * 商品标题
     */
    @JsonProperty("product_title")
    private String productTitle;

    /**
     * 商品图
     */
    @JsonProperty("product_img")
    private String productImg;

    /**
     * 商品单价
     */
    private BigDecimal amount;


}
