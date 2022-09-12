package cn.skyln.web.service;

import cn.skyln.web.model.REQ.CartItemRequest;
import cn.skyln.web.model.VO.CartVO;

/**
 * @Author: lamella
 * @Date: 2022/09/12/15:33
 * @Description:
 */
public interface CartService {
    /**
     * 添加商品到购物车
     *
     * @param cartItemRequest 加入购物车商品对象
     */
    void addToCart(CartItemRequest cartItemRequest);

    /**
     * 清空购物车
     */
    void clear();

    /**
     * 查看我的购物车
     *
     * @return CartVO
     */
    CartVO getMyCart();

    /**
     * 删除购物项
     *
     * @param productId 商品ID
     */
    void deleteItem(long productId);

    /**
     * 修改购物项
     *
     * @param cartItemRequest 购物车商品对象
     */
    void changeItem(CartItemRequest cartItemRequest);
}
