package cn.skyln.web.controller;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.CouponCategoryEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DTO.NewUserCouponDTO;
import cn.skyln.web.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author skylamella
 * @since 2022-09-07
 */
@Api(tags = "优惠券模块")
@RestController
@RequestMapping("/api/v1/coupon/")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @ApiOperation("分页查看优惠券")
    @GetMapping("page_coupon")
    public JsonData pageCoupon(@ApiParam(value = "第几页", required = true) @RequestParam(value = "page", defaultValue = "1") int page,
                               @ApiParam(value = "一页显示几条", required = true) @RequestParam(value = "size", defaultValue = "2") int size) {

        Map<String, Object> pageMap = couponService.pageCouponActivity(page, size);
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, pageMap);
    }

    @ApiOperation("领取优惠券")
    @GetMapping("/add/promotion/{coupon_id}")
    public JsonData addPromotionCoupon(@ApiParam(value = "优惠券ID", required = true) @PathVariable(value = "coupon_id") long couponId){
        return couponService.addCoupon(couponId, CouponCategoryEnum.PROMOTION);
    }

    @ApiOperation("RPC-新用户注册领券接口")
    @PostMapping("/add/new_user")
    public JsonData addNewUserCoupon(@ApiParam(value = "新用户注册领券对象", required = true) @RequestBody NewUserCouponDTO newUserCouponDTO){
        return couponService.intiNewUserCoupon(newUserCouponDTO);
    }
}

