package cn.skyln.coupon.web.service;

import cn.skyln.common.enums.CouponCategoryEnum;
import cn.skyln.common.utils.JsonData;
import cn.skyln.coupon.web.model.DO.CouponDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-07
 */
public interface CouponService extends IService<CouponDO> {
    /**
     * 分页查询优惠券
     *
     * @param page 第几页
     * @param size 一页显示几条
     * @return Map
     */
    Map<String, Object> pageCouponActivity(int page, int size);

    /**
     * 领取优惠券接口
     *
     * @param couponId  优惠券ID
     * @param promotion 优惠券类型
     * @return JsonData
     */
    JsonData addCoupon(long couponId, CouponCategoryEnum promotion);
}