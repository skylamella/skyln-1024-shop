package cn.skyln.web.controller;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.REQ.CartItemRequest;
import cn.skyln.web.model.VO.CartVO;
import cn.skyln.web.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public JsonData addProductToCart(@ApiParam(value = "加入购物车商品对象", required = true) @RequestBody CartItemRequest cartItemRequest){
        cartService.addToCart(cartItemRequest);
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    @ApiOperation("清空购物车")
    @PostMapping("clear")
    public JsonData clearCart(){
        cartService.clear();
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    @ApiOperation("查看我的购物车")
    @GetMapping("my_cart")
    public JsonData findMyCart(){
        CartVO cartVO = cartService.getMyCart();
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS,cartVO);
    }

    @ApiOperation("删除购物项")
    @PostMapping("delete/{product_id}")
    public JsonData deleteItem(@ApiParam(value = "商品ID", required = true) @PathVariable("product_id") long productId){
        cartService.deleteItem(productId);
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    @ApiOperation("修改购物项")
    @PostMapping("change")
    public JsonData changeItem(@ApiParam(value = "加入购物车商品对象", required = true) @RequestBody CartItemRequest cartItemRequest){
        cartService.changeItem(cartItemRequest);
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

}
