package cn.skyln.web.service.impl;

import cn.skyln.config.RabbitMQConfig;
import cn.skyln.constant.CacheKey;
import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.ProductOrderStateEnum;
import cn.skyln.exception.BizException;
import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.CartMessage;
import cn.skyln.model.LoginUser;
import cn.skyln.utils.CommonUtils;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.ProductOrderFeignService;
import cn.skyln.web.mapper.CartItemMapper;
import cn.skyln.web.mapper.CartMapper;
import cn.skyln.web.model.DO.CartDO;
import cn.skyln.web.model.DO.CartItemDO;
import cn.skyln.web.model.DTO.CartDTO;
import cn.skyln.web.model.REQ.CartItemRequest;
import cn.skyln.web.model.VO.CartItemVO;
import cn.skyln.web.model.VO.CartVO;
import cn.skyln.web.model.VO.ProductDetailVO;
import cn.skyln.web.service.CartService;
import cn.skyln.web.service.ProductService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: lamella
 * @Date: 2022/09/12/15:33
 * @Description:
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductOrderFeignService productOrderFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    /**
     * ????????????????????????
     *
     * @param cartItemRequest ???????????????????????????
     */
    @Override
    public void addToCart(CartItemRequest cartItemRequest) {
        Long productId = cartItemRequest.getProductId();
        int buyNum = cartItemRequest.getBuyNum();
        // ???????????????
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        Object cacheObj = myCart.get(String.valueOf(productId));
        String result = "";
        if (Objects.nonNull(cacheObj)) {
            result = (String) cacheObj;
        }
        if (StringUtils.isBlank(result)) {
            // ?????????????????????????????????
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
            cartItemVO.setDelStatue(false);
            myCart.put(String.valueOf(productId), JSON.toJSONString(cartItemVO));
        } else {
            // ???????????????????????????
            CartItemVO cartItemVO = JSON.parseObject(result, CartItemVO.class);
            cartItemVO.setBuyNum(cartItemVO.getBuyNum() + buyNum);
            myCart.put(String.valueOf(productId), JSON.toJSONString(cartItemVO));
        }
    }

    /**
     * ???????????????
     */
    @Override
    public void clear() {
        String cartKey = getCartKey();
        if (redisTemplate.hasKey(cartKey)) {
            BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
            List<Object> itemList = myCart.values();
            if (Objects.nonNull(itemList) && !itemList.isEmpty()) {
                itemList.forEach(item -> deleteItem((String) item, myCart));
            }
        } else {
            throw new BizException(BizCodeEnum.CART_NOT_EXIT);
        }
    }

    /**
     * ?????????????????????
     *
     * @return CartVO
     */
    @Override
    public CartVO getMyCart() {
        // ?????????????????????
        List<CartItemVO> cartItemVOList = buildCartItemList(false);

        // ?????????CartVO
        CartVO cartVO = new CartVO();
        cartVO.setCartItemVOList(cartItemVOList);
        return cartVO;
    }

    /**
     * ???????????????
     *
     * @param productId ??????ID
     */
    @Override
    public void deleteItem(long productId) {
        // ???????????????
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        deleteItem(productId, myCart);
    }

    private void deleteItem(long productId, long userId) {
        // ???????????????
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps(userId);
        deleteItem(productId, myCart);
    }

    private void deleteItem(long productId, BoundHashOperations<String, Object, Object> myCart) {
        deleteItem((String) myCart.get(String.valueOf(productId)), myCart);
    }

    private void deleteItem(String result, BoundHashOperations<String, Object, Object> myCart) {
        if (StringUtils.isNotBlank(result)) {
            CartItemVO cartItemVO = JSON.parseObject(result, CartItemVO.class);
            cartItemVO.setDelStatue(true);
            myCart.put(String.valueOf(cartItemVO.getProductId()), JSON.toJSONString(cartItemVO));
        }
    }

    /**
     * ???????????????
     *
     * @param cartItemRequest ?????????????????????
     */
    @Override
    public void changeItem(CartItemRequest cartItemRequest) {
        // ???????????????
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        Object object = myCart.get(String.valueOf(cartItemRequest.getProductId()));
        if (Objects.isNull(object)) {
            throw new BizException(BizCodeEnum.CART_NOT_EXIT);
        }
        CartItemVO cartItemVO = JSON.parseObject((String) object, CartItemVO.class);
        if (cartItemRequest.getBuyNum() < 0) {
            throw new BizException(BizCodeEnum.CART_UPD_NUM_FAIL);
        } else if (cartItemRequest.getBuyNum() == 0) {
            deleteItem(cartItemRequest.getProductId(), myCart);
        }
        cartItemVO.setBuyNum(cartItemRequest.getBuyNum());
        myCart.put(String.valueOf(cartItemRequest.getProductId()), JSON.toJSONString(cartItemVO));
    }

    @Override
    public List<CartItemVO> confirmOrderCartItems(CartDTO cartDTO) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        // ?????????????????????????????????
        List<CartItemVO> cartItemVOList = buildCartItemList(true);
        List<Long> productIdList = cartDTO.getProductIdList();
        // ?????????????????????id
        return cartItemVOList.stream().filter(obj -> {
            if (productIdList.contains(obj.getProductId())) {
                CartMessage cartMessage = new CartMessage();
                cartMessage.setOutTradeNo(cartDTO.getOrderOutTradeNo());
                cartMessage.setProductId(obj.getProductId());
                cartMessage.setUserId(loginUser.getId());
                rabbitTemplate.convertAndSend(rabbitMQConfig.getCartEventExchange(),
                        rabbitMQConfig.getCartReleaseDelayRoutingKey(),
                        cartMessage);
                log.info("???????????????-???????????????????????????{}", cartMessage);
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
    }

    /**
     * ??????MQ??????????????????
     *
     * @param cartMessage MQ?????????
     * @return ????????????
     */
    @Override
    public boolean cleanCartRecord(CartMessage cartMessage) {
        Long productId = cartMessage.getProductId();
        String outTradeNo = cartMessage.getOutTradeNo();
        JsonData jsonData = productOrderFeignService.queryProductOrderState(outTradeNo);
        if (jsonData.getCode() == 0) {
            // ?????????????????????????????????
            String state = jsonData.getData().toString();
            // ???????????????????????????????????????????????????????????????
            if (StringUtils.isBlank(state)) {
                log.warn("???????????????????????????????????????????????????????????????{}", cartMessage);
                return false;
            }
            // ?????????NEW???????????????????????????????????????????????????
            if (StringUtils.equalsIgnoreCase(ProductOrderStateEnum.NEW.name(), state)) {
                log.warn("???????????????NEW??????????????????????????????????????????{}", cartMessage);
                return false;
            }
            // ?????????????????????
            if (StringUtils.equalsIgnoreCase(ProductOrderStateEnum.PAY.name(), state)) {
                // ???????????????
                this.deleteItem(productId, cartMessage.getUserId());
                log.info("??????????????????????????????????????????????????????{}", cartMessage);
                return true;
            }
        }
        log.warn("??????????????????????????????????????????outTradeNo={}????????????{}", outTradeNo, cartMessage);
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param latestAmount ???????????????????????????
     * @return ???????????????
     */
    private List<CartItemVO> buildCartItemList(boolean latestAmount) {
        // ???????????????
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        List<Object> itemList = myCart.values();
        List<CartItemVO> cartItemVOList = new ArrayList<>();
        // ??????ID????????????????????????
        List<Long> productIdList = new ArrayList<>();
        if (Objects.nonNull(itemList) && !itemList.isEmpty()) {
            itemList.forEach(item -> {
                CartItemVO cartItemVO = JSON.parseObject((String) item, CartItemVO.class);
                if (!cartItemVO.isDelStatue()) {
                    cartItemVOList.add(cartItemVO);
                    productIdList.add(cartItemVO.getProductId());
                }
            });
        }
        if (latestAmount && !cartItemVOList.isEmpty() && !productIdList.isEmpty()) {
            setProductLatestAmount(cartItemVOList, productIdList);
        }
        return cartItemVOList;
    }

    /**
     * ????????????????????????
     *
     * @param cartItemVOList ???????????????
     * @param productIdList  ID??????
     */
    private void setProductLatestAmount(List<CartItemVO> cartItemVOList, List<Long> productIdList) {
        // ????????????
        List<ProductDetailVO> productDetailVOList = productService.findProductsByIdBatch(productIdList);
        // ??????ID??????
        Map<Long, ProductDetailVO> productDetailVOMap = productDetailVOList.stream().collect(Collectors.toMap(ProductDetailVO::getId, Function.identity()));

        cartItemVOList.forEach(item -> {
            ProductDetailVO productDetailVO = productDetailVOMap.get(item.getProductId());
            item.setAmount(productDetailVO.getAmount());
            item.setProductTitle(productDetailVO.getTitle());
            item.setProductImg(productDetailVO.getCoverImg());
        });
    }

    /**
     * redis??????????????????????????????mysql???
     *
     * @return ???????????????????????????
     */
    @Override
    public Integer redisCart2MysqlCart() {
        // ????????????????????????key
        Set<String> keys = redisTemplate.keys("cart:*");
        if (Objects.nonNull(keys) && !keys.isEmpty()) {
            keys.forEach(key -> {
                // ??????redis_cart_key????????????ID
                Long userId = Long.valueOf(key.split(":")[1]);
                // ????????????ID??????mysql?????????
                CartDO cartDO = cartMapper.selectOne(new QueryWrapper<CartDO>().eq("user_id", userId));
                if (Objects.isNull(cartDO)) {
                    // ??????????????????????????????????????????mysql?????????
                    cartDO = new CartDO();
                    cartDO.setUserId(userId);
                    cartMapper.insert(cartDO);
                }
                // ????????????ID??????redis?????????
                BoundHashOperations<String, Object, Object> cart = getMyCartOps(userId);
                // ??????redis?????????
                List<Object> itemList = cart.values();
                if (Objects.nonNull(itemList) && !itemList.isEmpty()) {
                    CartDO finalCartDO = cartDO;
                    List<CartItemDO> cartItemList = new ArrayList<>();
                    // ????????????redis???????????????
                    itemList.forEach(item -> {
                        CartItemVO cartItemVO = JSON.parseObject((String) item, CartItemVO.class);
                        CartItemDO cartItemDO = cartItemMapper.selectOne(new QueryWrapper<CartItemDO>()
                                .eq("product_id", cartItemVO.getProductId())
                                .eq("cart_id", finalCartDO.getId()));
                        // ????????????????????????????????????
                        if (cartItemVO.isDelStatue()) {
                            // ??????mysql??????????????????????????????mysql????????????
                            if (Objects.nonNull(cartItemDO)) {
                                cartItemMapper.deleteById(cartItemDO.getId());
                            }
                            // ???redis???????????????????????????
                            cart.delete(String.valueOf(cartItemVO.getProductId()));
                        } else {
                            ProductDetailVO productDetailVO = productService.findDetailById(cartItemVO.getProductId());
                            cartItemVO.setAmount(productDetailVO.getAmount());
                            cartItemVO.setProductTitle(productDetailVO.getTitle());
                            cartItemVO.setProductImg(productDetailVO.getCoverImg());
                            cart.put(String.valueOf(cartItemVO.getProductId()), JSON.toJSONString(cartItemVO));
                            if (Objects.isNull(cartItemDO)) {
                                // ????????????????????????????????????????????????????????????
                                cartItemDO = (CartItemDO) CommonUtils.beanProcess(cartItemVO, new CartItemDO());
                                cartItemDO.setCartId(finalCartDO.getId());
                                cartItemList.add(cartItemDO);
                            } else {
                                BeanUtils.copyProperties(cartItemVO, cartItemDO);
                                cartItemMapper.updateById(cartItemDO);
                            }
                        }
                    });
                    // ??????????????????????????????mysql
                    cartItemMapper.insertBatch(cartItemList);
                }
            });
            return 1;
        }
        return 0;
    }

    /**
     * ???????????????redis???key
     *
     * @return
     */
    private String getCartKey() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        return String.format(CacheKey.CART_KEY, loginUser.getId());
    }

    /**
     * ???????????????
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getMyCartOps() {
        String cartKey = getCartKey();
        return redisTemplate.boundHashOps(cartKey);
    }

    private BoundHashOperations<String, Object, Object> getMyCartOps(Long userId) {
        String cartKey = String.format(CacheKey.CART_KEY, userId);
        return redisTemplate.boundHashOps(cartKey);
    }
}
