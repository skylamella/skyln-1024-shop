package cn.skyln.coupon.web.mapper;

import cn.skyln.coupon.web.model.DO.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author skylamella
 * @since 2022-09-07
 */
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 扣减库存
     * @param couponId 优惠券ID
     * @return 影响行数
     */
    int reduceStock(long couponId);
}
