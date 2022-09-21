package cn.skyln.web.feignClient;

import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.impl.ProductOrderFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: lamella
 * @Date: 2022/09/20/21:54
 * @Description:
 */
@FeignClient(name = "skyln-order-service", fallback = ProductOrderFeignServiceFallback.class)
public interface ProductOrderFeignService {

    /**
     * 查询订单状态
     *
     * @param outTradeNo 订单号
     * @return JsonData
     */
    @GetMapping("/api/v1/order/query_state")
    JsonData queryProductOrderState(@RequestParam("out_trade_no") String outTradeNo);
}
