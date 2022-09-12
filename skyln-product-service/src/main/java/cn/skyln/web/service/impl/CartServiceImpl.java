package cn.skyln.web.service.impl;

import cn.skyln.constant.CacheKey;
import cn.skyln.enums.BizCodeEnum;
import cn.skyln.exception.BizException;
import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.LoginUser;
import cn.skyln.web.model.REQ.CartItemRequest;
import cn.skyln.web.model.VO.CartItemVO;
import cn.skyln.web.model.VO.CartVO;
import cn.skyln.web.model.VO.ProductDetailVO;
import cn.skyln.web.service.CartService;
import cn.skyln.web.service.ProductService;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            ProductDetailVO productDetail = productService.findDetailById(productId);
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
     * 清空购物车
     */
    @Override
    public void clear() {
        String cartKey = getCartKey();
        if (redisTemplate.hasKey(cartKey)) {
            redisTemplate.delete(cartKey);
        } else {
            throw new BizException(BizCodeEnum.CART_NOT_EXIT);
        }
    }

    /**
     * 查看我的购物车
     *
     * @return CartVO
     */
    @Override
    public CartVO getMyCart() {
        // 获取全部购物项
        List<CartItemVO> cartItemVOList = buildCartItemList(false);

        // 封装成CartVO
        CartVO cartVO = new CartVO();
        cartVO.setCartItemVOList(cartItemVOList);
        return cartVO;
    }

    /**
     * 删除购物项
     *
     * @param productId 商品ID
     */
    @Override
    public void deleteItem(long productId) {
        // 获取购物车
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        myCart.delete(String.valueOf(productId));
    }

    /**
     * 修改购物项
     *
     * @param cartItemRequest 购物车商品对象
     */
    @Override
    public void changeItem(CartItemRequest cartItemRequest) {
        // 获取购物车
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        Object object = myCart.get(String.valueOf(cartItemRequest.getProductId()));
        if (Objects.isNull(object)) {
            throw new BizException(BizCodeEnum.CART_NOT_EXIT);
        }
        CartItemVO cartItemVO = JSON.parseObject((String) object, CartItemVO.class);
        if (cartItemRequest.getBuyNum() < 0) {
            throw new BizException(BizCodeEnum.CART_UPD_NUM_FAIL);
        } else if (cartItemRequest.getBuyNum() == 0) {
            deleteItem(cartItemRequest.getProductId());
        }
        cartItemVO.setBuyNum(cartItemRequest.getBuyNum());
        myCart.put(String.valueOf(cartItemRequest.getProductId()), JSON.toJSONString(cartItemVO));
    }

    /**
     * 获取最新的购物项
     *
     * @param latestAmount 是否获取最新的价格
     * @return 购物项列表
     */
    private List<CartItemVO> buildCartItemList(boolean latestAmount) {
        // 获取购物车
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        List<Object> itemList = myCart.values();
        List<CartItemVO> cartItemVOList = new ArrayList<>();
        // 拼接ID列表查询最新价格
        List<Long> productIdList = new ArrayList<>();
        for (Object item : itemList) {
            CartItemVO cartItemVO = JSON.parseObject((String) item, CartItemVO.class);
            cartItemVOList.add(cartItemVO);
            productIdList.add(cartItemVO.getProductId());
        }
        if (latestAmount) {
            setProductLatestAmount(cartItemVOList, productIdList);
        }
        return cartItemVOList;
    }

    /**
     * 设置商品最新价格
     *
     * @param cartItemVOList 购物项列表
     * @param productIdList  ID列表
     */
    private void setProductLatestAmount(List<CartItemVO> cartItemVOList, List<Long> productIdList) {
        // 批量查询
        List<ProductDetailVO> productDetailVOList = productService.findProductsByIdBatch(productIdList);
        // 根据ID分组
        Map<Long, ProductDetailVO> productDetailVOMap = productDetailVOList.stream().collect(Collectors.toMap(ProductDetailVO::getId, Function.identity()));

        cartItemVOList.stream().forEach(item -> {
            ProductDetailVO productDetailVO = productDetailVOMap.get(item.getProductId());
            item.setAmount(productDetailVO.getAmount());
            item.setProductTitle(productDetailVO.getTitle());
            item.setProductImg(productDetailVO.getCoverImg());
        });
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
