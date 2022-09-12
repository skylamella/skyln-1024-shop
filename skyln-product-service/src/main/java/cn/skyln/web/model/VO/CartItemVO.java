package cn.skyln.web.model.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: lamella
 * @Date: 2022/09/12/14:58
 * @Description: 购物项
 */
public class CartItemVO {
    /**
     * 商品ID
     */
    @JsonProperty("product_id")
    private long productId;

    /**
     * 商品数量
     */
    @JsonProperty("buy_num")
    private Integer buyNum;

    /**
     * 商品标题
     */
    @JsonProperty("product_title")
    private String productTitle;

    /**
     * 商品封面图
     */
    @JsonProperty("product_img")
    private String productImg;

    /**
     * 商品单价
     */
    private BigDecimal amount;

    /**
     * 商品总价，单价*数量
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public Integer getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(Integer buyNum) {
        this.buyNum = buyNum;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductImg() {
        return productImg;
    }

    public void setProductImg(String productImg) {
        this.productImg = productImg;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTotalAmount() {
        return this.amount.multiply(new BigDecimal(this.buyNum));
    }
}
