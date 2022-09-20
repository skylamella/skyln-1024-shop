package cn.skyln.web.service;

import cn.skyln.model.CouponRecordMessage;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DO.CouponRecordDO;
import cn.skyln.web.model.REQ.LockCouponRecordRequest;
import cn.skyln.web.model.VO.CouponRecordVO;
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

    /**
     * 根据ID查询优惠券记录详情
     *
     * @param couponRecordId 记录ID
     * @return CouponRecordVO
     */
    CouponRecordVO getOneById(long couponRecordId);

    /**
     * 锁定优惠券
     *
     * @param lockCouponRecordRequest 锁定优惠券请求对象
     * @return JsonData
     */
    JsonData lockCouponRecord(LockCouponRecordRequest lockCouponRecordRequest);

    /**
     *
     * @param couponRecordMessage
     * @return
     */
    boolean releaseCouponRecord(CouponRecordMessage couponRecordMessage);
}
