package cn.skyln.web.model.VO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @Author: lamella
 * @Date: 2022/09/12/14:58
 * @Description: 购物车
 */
public class CartVO {

    /**
     * 购物车中的购物项详情
     */
    @JsonProperty("cart_itemVO_list")
    private List<CartItemVO> cartItemVOList;

    /**
     * 商品总件数
     */
    @JsonProperty("cart_item_num")
    private Integer cartItemNum;

    /**
     * 整个购物车总价
     */
    @JsonProperty("total_cart_amount")
    private BigDecimal totalCartAmount;


    /**
     * 实际支付总价
     */
    @JsonProperty("real_pay_amount")
    private BigDecimal realPayAmount;

    /**
     * 总件数
     *
     * @return
     */
    public Integer getCartItemNum() {
        if (Objects.nonNull(this.cartItemVOList)) {
            return cartItemVOList.stream().mapToInt(CartItemVO::getBuyNum).sum();
        }
        return 0;
    }

    /**
     * 总价格
     *
     * @return
     */
    public BigDecimal getCartAmount() {
        BigDecimal amount = new BigDecimal("0");
        if (Objects.nonNull(this.cartItemVOList)) {
            for (CartItemVO cartItemVO : cartItemVOList) {
                BigDecimal itemTotalAmount = cartItemVO.getTotalAmount();
                amount = amount.add(itemTotalAmount);
            }
        }
        return amount;
    }

    /**
     * 购物车里面实际支付的价格
     *
     * @return BigDecimal
     */
    public BigDecimal getRealPayAmount() {
        // todo
        BigDecimal amount = new BigDecimal("0");
        if (Objects.nonNull(this.cartItemVOList)) {
            for (CartItemVO cartItemVO : cartItemVOList) {
                BigDecimal itemTotalAmount = cartItemVO.getTotalAmount();
                amount = amount.add(itemTotalAmount);
            }
        }
        return amount;
    }

    public List<CartItemVO> getCartItemVOList() {
        return cartItemVOList;
    }

    public void setCartItemVOList(List<CartItemVO> cartItemVOList) {
        this.cartItemVOList = cartItemVOList;
    }

}
