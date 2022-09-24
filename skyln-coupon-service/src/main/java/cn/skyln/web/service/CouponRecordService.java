package cn.skyln.web.service;

import cn.skyln.model.CouponRecordMessage;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DO.CouponRecordDO;
import cn.skyln.web.model.DTO.CouponDTO;
import cn.skyln.web.model.DTO.LockCouponRecordDTO;
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
     * @param lockCouponRecordDTO 锁定优惠券请求对象
     * @return JsonData
     */
    JsonData lockCouponRecord(LockCouponRecordDTO lockCouponRecordDTO);

    /**
     * 解锁优惠券记录
     *
     * @param couponRecordMessage CouponRecordMessage
     * @return 解锁状态
     */
    boolean releaseCouponRecord(CouponRecordMessage couponRecordMessage);

    /**
     * 根据ID列表获取优惠券详情并锁定优惠券
     *
     * @param couponDTO CouponDTO
     * @return JsonData
     */
    JsonData queryUserCouponRecord(CouponDTO couponDTO);
}
