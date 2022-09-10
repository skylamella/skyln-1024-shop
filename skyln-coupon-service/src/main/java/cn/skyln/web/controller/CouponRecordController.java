package cn.skyln.web.controller;


import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.service.CouponRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
}

