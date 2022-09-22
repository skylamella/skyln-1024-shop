package cn.skyln.web.feignClient;

import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.fallBack.UserFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Author: lamella
 * @Date: 2022/09/22/22:09
 * @Description:
 */
@FeignClient(name = "skyln-user-service", fallback = UserFeignServiceFallback.class)
public interface UserFeignService {

    /**
     * 查询用户收货地址详情
     *
     * @param addressId 收货地址ID
     * @return JsonData
     */
    @PostMapping("/api/v1/address/find/{address_id}")
    JsonData getOneAddress(@PathVariable("address_id") long addressId);
}
