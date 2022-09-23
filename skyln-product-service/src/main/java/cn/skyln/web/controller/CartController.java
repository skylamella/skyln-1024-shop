package cn.skyln.web.controller;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DTO.CartDTO;
import cn.skyln.web.model.REQ.CartItemRequest;
import cn.skyln.web.model.VO.CartItemVO;
import cn.skyln.web.model.VO.CartVO;
import cn.skyln.web.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @Author: lamella
 * @Date: 2022/09/12/15:31
 * @Description:
 */
@Api(tags = "购物车模块")
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @ApiOperation("根据ID查看商品详情")
    @PostMapping("add")
    public JsonData addProductToCart(@ApiParam(value = "加入购物车商品对象", required = true) @RequestBody CartItemRequest cartItemRequest) {
        cartService.addToCart(cartItemRequest);
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    @ApiOperation("清空购物车")
    @PostMapping("clear")
    public JsonData clearCart() {
        cartService.clear();
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    @ApiOperation("查看我的购物车")
    @GetMapping("my_cart")
    public JsonData findMyCart() {
        CartVO cartVO = cartService.getMyCart();
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, cartVO);
    }

    @ApiOperation("删除购物项")
    @PostMapping("delete/{product_id}")
    public JsonData deleteItem(@ApiParam(value = "商品ID", required = true) @PathVariable("product_id") long productId) {
        cartService.deleteItem(productId);
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    @ApiOperation("修改购物项")
    @PostMapping("change")
    public JsonData changeItem(@ApiParam(value = "加入购物车商品对象", required = true) @RequestBody CartItemRequest cartItemRequest) {
        cartService.changeItem(cartItemRequest);
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    /**
     * 用于订单服务，确认订单，获取对应的商品详情信息
     * 会清空对应的购物车商品数据
     *
     * @param cartDTO CartDTO
     * @return JsonData
     */
    @ApiOperation("获取对应订单的商品详情信息")
    @PostMapping("confirm_order_cart_items")
    public JsonData confirmOrderCartItems(@ApiParam(value = "商品ID列表", required = true) @RequestBody CartDTO cartDTO) {
        List<CartItemVO> cartItemVOList = cartService.confirmOrderCartItems(cartDTO);
        if(cartItemVOList.size() == 0){
            return JsonData.returnJson(BizCodeEnum.CART_NOT_EXIT);
        }
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, cartItemVOList);
    }

}
