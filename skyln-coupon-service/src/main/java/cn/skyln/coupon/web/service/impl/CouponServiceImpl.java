package cn.skyln.coupon.web.service.impl;

import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.enums.CouponCategoryEnum;
import cn.skyln.common.enums.CouponPublishEnum;
import cn.skyln.common.enums.CouponUseStateEnum;
import cn.skyln.common.exceptions.BizException;
import cn.skyln.common.interceptor.LoginInterceptor;
import cn.skyln.common.model.LoginUser;
import cn.skyln.common.utils.CommonUtils;
import cn.skyln.common.utils.JsonData;
import cn.skyln.coupon.web.mapper.CouponMapper;
import cn.skyln.coupon.web.mapper.CouponRecordMapper;
import cn.skyln.coupon.web.model.DO.CouponDO;
import cn.skyln.coupon.web.model.DO.CouponRecordDO;
import cn.skyln.coupon.web.model.VO.CouponVO;
import cn.skyln.coupon.web.service.CouponService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-07
 */
@Service
@Slf4j
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponDO> implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private CouponRecordMapper couponRecordMapper;

    /**
     * 分页查询优惠券
     *
     * @param page 第几页
     * @param size 一页显示几条
     * @return Map
     */
    @Override
    public Map<String, Object> pageCouponActivity(int page, int size) {
        Page<CouponDO> pageInfo = new Page<>(page, size);
        IPage<CouponDO> couponDOIPage = couponMapper.selectPage(pageInfo, new QueryWrapper<CouponDO>()
                .eq("publish", CouponPublishEnum.PUBLISH)
                .eq("category", CouponCategoryEnum.PROMOTION)
                .orderByDesc("create_time"));

        Map<String, Object> pageMap = new HashMap<>();
        pageMap.put("total_record", couponDOIPage.getTotal());
        pageMap.put("total_page", couponDOIPage.getPages());
        pageMap.put("current_data", couponDOIPage.getRecords().stream().map(this::beanProcess).collect(Collectors.toList()));
        return pageMap;
    }

    /**
     * 领取优惠券接口
     * 1、获取优惠券是否存在
     * 2、校验优惠券是否可以领取：时间、库存、超过限制
     * 3、扣减库存
     * 4、保存领券记录
     *
     * @param couponId  优惠券ID
     * @param promotion 优惠券类型
     * @return JsonData
     */
    @Override
    public JsonData addCoupon(long couponId, CouponCategoryEnum promotion) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        CouponDO couponDO = couponMapper.selectOne(new QueryWrapper<CouponDO>()
                .eq("id", couponId)
                .eq("category", promotion.name())
                .eq("publish", CouponPublishEnum.PUBLISH.name()));
        // 判断优惠券是否可以领取
        checkCoupon(couponDO, loginUser.getId());

        // 构建领券记录
        CouponRecordDO couponRecordDO = new CouponRecordDO();
        BeanUtils.copyProperties(couponDO, couponRecordDO);
        couponRecordDO.setCreateTime(new Date());
        couponRecordDO.setUseState(CouponUseStateEnum.NEW.name());
        couponRecordDO.setUserId(loginUser.getId());
        couponRecordDO.setUserName(loginUser.getName());
        couponRecordDO.setCouponId(couponId);
        couponRecordDO.setId(null);

        // 扣减库存 todo
        int rows = couponMapper.reduceStock(couponId);

        if (rows == 1) {
            // 库存扣减成功才保存记录
            couponRecordMapper.insert(couponRecordDO);
        } else {
            log.error("发放优惠券失败：{}，用户：{}", couponDO, loginUser);
            throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
        }
        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
    }

    private Object beanProcess(CouponDO couponDO) {
        CouponVO couponVO = new CouponVO();
        BeanUtils.copyProperties(couponDO, couponVO);
        return couponVO;
    }

    /**
     * 判断登录用户是否可以领取优惠券
     * 1、优惠券库存是否足够
     * 2、是否在优惠券领取时间范围
     * 3、判断用户是否超过领取限制
     *
     * @param couponDO 优惠券
     * @param userId   用户ID
     */
    private void checkCoupon(CouponDO couponDO, long userId) {

        if (Objects.isNull(couponDO)) {
            throw new BizException(BizCodeEnum.COUPON_NO_EXITS);
        }

        // 优惠券库存是否足够
        if (couponDO.getStock() <= 0) {
            throw new BizException(BizCodeEnum.COUPON_NO_STOCK);
        }

        // 是否在优惠券领取时间范围
        long time = CommonUtils.getCurrentTimeStamp();
        long start = couponDO.getStartTime().getTime();
        long end = couponDO.getEndTime().getTime();
        if (time < start || time > end) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_TIME);
        }

        // 判断用户是否超过领取限制
        int recordNum = couponRecordMapper.selectCount(new QueryWrapper<CouponRecordDO>()
                .eq("coupon_id", couponDO.getId())
                .eq("user_id", userId));
        if (recordNum >= couponDO.getUserLimit()) {
            throw new BizException(BizCodeEnum.COUPON_OUT_OF_LIMIT);
        }

    }
}
