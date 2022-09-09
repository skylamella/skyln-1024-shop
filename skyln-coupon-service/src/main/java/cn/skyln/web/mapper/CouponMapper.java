package cn.skyln.web.mapper;

import cn.skyln.web.model.DO.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author skylamella
 * @since 2022-09-07
 */
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 扣减库存
     *
     * @param couponId 优惠券ID
     * @param version  版本号
     * @return 影响行数
     */
    int reduceStock(@Param("couponId") long couponId, @Param("version") long version);
}
