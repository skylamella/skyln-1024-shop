package cn.skyln.web.feignClient.fallBack;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.CouponFeignService;
import cn.skyln.web.model.REQ.NewUserCouponRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: lamella
 * @Date: 2022/09/10/20:03
 * @Description:
 */
@Slf4j
@Service
public class CouponFeignServiceFallback implements CouponFeignService {
    @Override
    public JsonData intiNewUserCoupon(NewUserCouponRequest newUserCouponRequest) {
        return JsonData.returnJson(BizCodeEnum.SYSTEM_ERROR);
    }
}
