package cn.skyln.web.service;

import cn.skyln.web.model.REQ.CartItemRequest;

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
}
