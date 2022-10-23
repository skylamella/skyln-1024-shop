package cn.skyln.feignClient;

import cn.skyln.feignClient.fallBack.ProductFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Author: lamella
 * @Date: 2022/09/23/21:44
 * @Description:
 */
@FeignClient(name = "skyln-product-service", fallback = ProductFeignServiceFallback.class)
public interface ProductFeignService {

    /**
     * 获取购物车的最新商品价格并清空对应的购物车商品
     */
    @PostMapping("/api/v1/cart/redis_cart_to_mysql")
    int redisCart2MysqlCart();
}
