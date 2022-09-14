package cn.skyln.web.feignClient;

import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.fallBack.CouponFeignServiceFallback;
import cn.skyln.web.model.REQ.NewUserCouponRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Author: lamella
 * @Date: 2022/09/10/20:03
 * @Description:
 */
@FeignClient(name = "skyln-coupon-service", fallback = CouponFeignServiceFallback.class)
public interface CouponFeignService {
    /**
     * 新用户注册发放优惠券
     *
     * @param newUserCouponRequest 新用户注册领券对象
     * @return JsonData
     */
    @PostMapping("/api/v1/coupon/add/new_user")
    JsonData intiNewUserCoupon(NewUserCouponRequest newUserCouponRequest);
}
