package cn.skyln.web.mapper;

import cn.skyln.web.model.DO.CouponRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author skylamella
 * @since 2022-09-07
 */
public interface CouponRecordMapper extends BaseMapper<CouponRecordDO> {

    /**
     * 批量更新优惠券使用记录
     *
     * @param userId              用户ID
     * @param useState            更新状态
     * @param lockCouponRecordIds 优惠券ID列表
     * @return 影响行数
     */
    int lockUseStateBatch(@Param("userId") Long userId, @Param("useState") String useState, @Param("lockCouponRecordIds") List<Long> lockCouponRecordIds);

    /**
     * 更新优惠券使用记录
     *
     * @param couponRecordId 优惠券记录id
     * @param useState       更新状态
     * @return 影响行数
     */
    int updateState(@Param("couponRecordId") Long couponRecordId, @Param("useState") String useState);
}
