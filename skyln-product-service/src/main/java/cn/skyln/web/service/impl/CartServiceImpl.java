package cn.skyln.web.service.impl;

import cn.skyln.constant.CacheKey;
import cn.skyln.enums.BizCodeEnum;
import cn.skyln.exception.BizException;
import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.LoginUser;
import cn.skyln.web.model.REQ.CartItemRequest;
import cn.skyln.web.model.VO.CartItemVO;
import cn.skyln.web.model.VO.ProductDetailVO;
import cn.skyln.web.service.CartService;
import cn.skyln.web.service.ProductService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author: lamella
 * @Date: 2022/09/12/15:33
 * @Description:
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductService productService;

    /**
     * 添加商品到购物车
     *
     * @param cartItemRequest 加入购物车商品对象
     */
    @Override
    public void addToCart(CartItemRequest cartItemRequest) {
        Long productId = cartItemRequest.getProductId();
        int buyNum = cartItemRequest.getBuyNum();
        // 获取购物车
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        Object cacheObj = myCart.get(String.valueOf(productId));
        String result = "";
        if (Objects.nonNull(cacheObj)) {
            result = (String) cacheObj;
        }
        if (StringUtils.isBlank(result)) {
            // 不存在商品，新增购物项
            CartItemVO cartItemVO = new CartItemVO();
            ProductDetailVO productDetail = productService.findDetailById(cartItemRequest.getProductId());
            if (Objects.isNull(productDetail)) {
                throw new BizException(BizCodeEnum.PRODUCT_NOT_EXIT);
            }
            cartItemVO.setProductId(productId);
            cartItemVO.setBuyNum(buyNum);
            cartItemVO.setProductImg(productDetail.getCoverImg());
            cartItemVO.setProductTitle(productDetail.getTitle());
            cartItemVO.setAmount(productDetail.getAmount());
            myCart.put(String.valueOf(productId), JSON.toJSONString(cartItemVO));
        } else {
            // 存在商品，修改数量
            CartItemVO cartItemVO = JSON.parseObject(result, CartItemVO.class);
            cartItemVO.setBuyNum(cartItemVO.getBuyNum() + buyNum);
            myCart.put(String.valueOf(productId), JSON.toJSONString(cartItemVO));
        }
    }

    /**
     * 获取购物车redis的key
     *
     * @return
     */
    private String getCartKey() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        return String.format(CacheKey.CART_KEY, loginUser.getId());
    }

    /**
     * 我的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getMyCartOps() {
        String cartKey = getCartKey();
        return redisTemplate.boundHashOps(cartKey);
    }
}
