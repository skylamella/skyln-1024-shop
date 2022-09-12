package cn.skyln.web.controller;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.REQ.CartItemRequest;
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

}
