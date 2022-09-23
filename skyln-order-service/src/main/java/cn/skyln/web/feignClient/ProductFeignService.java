package cn.skyln.web.feignClient;

import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.fallBack.ProductFeignServiceFallback;
import cn.skyln.web.model.DTO.CartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: lamella
 * @Date: 2022/09/23/21:44
 * @Description:
 */
@FeignClient(name = "skyln-product-service", fallback = ProductFeignServiceFallback.class)
public interface ProductFeignService {

    /**
     * 获取购物车的最新商品价格并清空对应的购物车商品
     *
     * @param cartDTO CartDTO
     * @return JsonData
     */
    @PostMapping("/api/v1/cart/confirm_order_cart_items")
    JsonData confirmOrderCartItem(@RequestBody CartDTO cartDTO);
}
