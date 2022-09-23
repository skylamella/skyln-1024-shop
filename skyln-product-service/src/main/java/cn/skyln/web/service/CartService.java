package cn.skyln.web.service;

import cn.skyln.model.CartMessage;
import cn.skyln.web.model.DTO.CartDTO;
import cn.skyln.web.model.REQ.CartItemRequest;
import cn.skyln.web.model.VO.CartItemVO;
import cn.skyln.web.model.VO.CartVO;

import java.util.List;

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

    /**
     * 用于订单服务，确认订单，获取对应的商品详情信息
     * 会清空对应的购物车商品数据
     *
     * @param cartDTO CartDTO
     * @return List<CartItemVO>
     */
    List<CartItemVO> confirmOrderCartItems(CartDTO cartDTO);

    /**
     * 清空MQ中的购物车项
     *
     * @param cartMessage MQ消息体
     * @return 清理结果
     */
    boolean cleanCartRecord(CartMessage cartMessage);
}
