package cn.skyln.web.mapper;

import cn.skyln.web.model.DO.CouponTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author skylamella
 * @since 2022-09-19
 */
public interface CouponTaskMapper extends BaseMapper<CouponTaskDO> {

    /**
     * 批量插入
     * @param couponTaskDOList
     * @return 影响行数
     */
    int insertBatch(@Param("couponTaskList") List<CouponTaskDO> couponTaskDOList);
}
