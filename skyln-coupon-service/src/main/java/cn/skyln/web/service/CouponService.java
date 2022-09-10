package cn.skyln.web.service;

import cn.skyln.enums.CouponCategoryEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DO.CouponDO;
import cn.skyln.web.model.REQ.NewUserCouponRequest;
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

    /**
     * 新用户注册发放优惠券
     *
     * @param newUserCouponRequest 新用户注册领券对象
     * @return JsonData
     */
    JsonData intiNewUserCoupon(NewUserCouponRequest newUserCouponRequest);
}
