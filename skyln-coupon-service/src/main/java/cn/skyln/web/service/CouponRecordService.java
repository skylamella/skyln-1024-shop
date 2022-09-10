package cn.skyln.web.service;

import cn.skyln.web.model.DO.CouponRecordDO;
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
public interface CouponRecordService extends IService<CouponRecordDO> {

    /**
     * 分页查询优惠券
     *
     * @param page     第几页
     * @param size     一页显示几条
     * @param useState 查询类型
     * @return Map
     */
    Map<String, Object> pageCouponActivity(int page, int size, String useState);
}
