package cn.skyln.web.controller;


import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DTO.CouponDTO;
import cn.skyln.web.model.DTO.LockCouponRecordDTO;
import cn.skyln.web.model.VO.CouponRecordVO;
import cn.skyln.web.service.CouponRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author skylamella
 * @since 2022-09-07
 */
@Api(tags = "优惠券领取记录模块")
@RestController
@RequestMapping("/api/v1/coupon_record/")
public class CouponRecordController {

    @Autowired
    private CouponRecordService couponRecordService;

    @ApiOperation("分页查询个人优惠券列表")
    @GetMapping("list")
    public JsonData pageList(@ApiParam(value = "第几页", required = true) @RequestParam(value = "page", defaultValue = "1") int page,
                             @ApiParam(value = "一页显示几条", required = true) @RequestParam(value = "size", defaultValue = "2") int size,
                             @ApiParam(value = "查询类型", required = true) @RequestParam(value = "use_state", defaultValue = "ALL") String useState) {
        Map<String, Object> pageMap = couponRecordService.pageCouponActivity(page, size, useState);
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, pageMap);
    }

    @ApiOperation("查询优惠券记录详情")
    @GetMapping("detail/{coupon_record_id}")
    public JsonData couponRecordDetail(@ApiParam(value = "记录ID", required = true) @PathVariable(value = "coupon_record_id") long couponRecordId){
        CouponRecordVO couponRecordVO = couponRecordService.getOneById(couponRecordId);
        if (Objects.isNull(couponRecordVO)) {
            return JsonData.returnJson(BizCodeEnum.COUPON_NO_EXITS);
        }
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, couponRecordVO);
    }

    @ApiOperation("RPC-锁定优惠券记录")
    @PostMapping("lock_records")
    public JsonData lockCouponRecord(@ApiParam(value = "锁定优惠券请求对象", required = true) @RequestBody LockCouponRecordDTO lockCouponRecordDTO){
        return couponRecordService.lockCouponRecord(lockCouponRecordDTO);
    }

    @ApiOperation("RPC-锁定优惠券记录")
    @PostMapping("detail/list")
    public JsonData queryUserCouponRecord(@ApiParam(value = "锁定优惠券请求对象", required = true) @RequestBody CouponDTO couponDTO){
        return couponRecordService.queryUserCouponRecord(couponDTO);
    }
}

