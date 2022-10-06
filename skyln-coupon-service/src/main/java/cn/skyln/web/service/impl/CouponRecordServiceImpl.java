package cn.skyln.web.service.impl;

import cn.skyln.config.RabbitMQConfig;
import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.CouponUseStateEnum;
import cn.skyln.enums.ProductOrderStateEnum;
import cn.skyln.enums.StockTaskStateEnum;
import cn.skyln.exception.BizException;
import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.CouponRecordMessage;
import cn.skyln.model.LoginUser;
import cn.skyln.utils.CommonUtils;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.ProductOrderFeignService;
import cn.skyln.web.mapper.CouponRecordMapper;
import cn.skyln.web.mapper.CouponTaskMapper;
import cn.skyln.web.model.DO.CouponRecordDO;
import cn.skyln.web.model.DO.CouponTaskDO;
import cn.skyln.web.model.DTO.CouponDTO;
import cn.skyln.web.model.DTO.LockCouponRecordDTO;
import cn.skyln.web.model.VO.CouponRecordVO;
import cn.skyln.web.service.CouponRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
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
public class CouponRecordServiceImpl extends ServiceImpl<CouponRecordMapper, CouponRecordDO> implements CouponRecordService {

    @Autowired
    private CouponRecordMapper couponRecordMapper;

    @Autowired
    private CouponTaskMapper couponTaskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private ProductOrderFeignService productOrderFeignService;

    /**
     * 分页查询优惠券
     *
     * @param page     第几页
     * @param size     一页显示几条
     * @param useState 查询类型
     * @return Map
     */
    @Override
    public Map<String, Object> pageCouponActivity(int page, int size, String useState) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        Page<CouponRecordDO> pageInfo = new Page<>(page, size);
        IPage<CouponRecordDO> recordDOPage;
        if (StringUtils.equals("ALL", useState)) {
            recordDOPage = couponRecordMapper.selectPage(pageInfo, new QueryWrapper<CouponRecordDO>()
                    .eq("user_id", loginUser.getId())
                    .orderByDesc("create_time"));
        } else {
            recordDOPage = couponRecordMapper.selectPage(pageInfo, new QueryWrapper<CouponRecordDO>()
                    .eq("user_id", loginUser.getId())
                    .eq("use_state", useState)
                    .orderByDesc("create_time"));
        }
        return CommonUtils.getReturnPageMap(recordDOPage.getTotal(),
                recordDOPage.getPages(),
                recordDOPage.getRecords().stream().map(obj ->
                                CommonUtils.beanProcess(obj, new CouponRecordVO()))
                        .collect(Collectors.toList()));
    }

    /**
     * 根据ID查询优惠券记录详情
     *
     * @param couponRecordId 记录ID
     * @return CouponRecordVO
     */
    @Override
    public CouponRecordVO getOneById(long couponRecordId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        CouponRecordDO couponRecordDO = couponRecordMapper.selectOne(new QueryWrapper<CouponRecordDO>()
                .eq("id", couponRecordId)
                .eq("user_id", loginUser.getId()));
        if (Objects.isNull(couponRecordDO)) {
            return null;
        }
        CouponRecordVO couponRecordVO = new CouponRecordVO();
        BeanUtils.copyProperties(couponRecordDO, couponRecordVO);
        return couponRecordVO;
    }

    /**
     * 锁定优惠券
     *
     * @param lockCouponRecordDTO 锁定优惠券请求对象
     * @return JsonData
     */
    @Override
    public JsonData lockCouponRecord(LockCouponRecordDTO lockCouponRecordDTO) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        List<Long> lockCouponRecordIds = lockCouponRecordDTO.getLockCouponRecordIds();
        String orderOutTradeNo = lockCouponRecordDTO.getOrderOutTradeNo();

        int updateRows = couponRecordMapper.lockUseStateBatch(loginUser.getId(), CouponUseStateEnum.USED.name(), lockCouponRecordIds);
        log.info("优惠券记录锁定updateRows={}", updateRows);
        List<CouponTaskDO> couponTaskDOList = lockCouponRecordIds.stream().map(obj -> {
            CouponTaskDO couponTaskDO = new CouponTaskDO();
            couponTaskDO.setCreateTime(new Date());
            couponTaskDO.setCouponRecordId(obj);
            couponTaskDO.setOutTradeNo(orderOutTradeNo);
            couponTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
            return couponTaskDO;
        }).collect(Collectors.toList());
        int insertRows = couponTaskMapper.insertBatch(couponTaskDOList);
        log.info("新增优惠券记录task，insertRows={}", insertRows);
        if (lockCouponRecordIds.size() == updateRows && lockCouponRecordIds.size() == insertRows) {
            // 发送延迟消息
            for (CouponTaskDO couponTaskDO : couponTaskDOList) {
                CouponRecordMessage couponRecordMessage = new CouponRecordMessage();
                couponRecordMessage.setOutTradeNo(orderOutTradeNo);
                couponRecordMessage.setTaskId(couponTaskDO.getId());
                rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(),
                        rabbitMQConfig.getCouponReleaseDelayRoutingKey(),
                        couponRecordMessage);
                log.info("优惠券锁定延迟消息发送成功：{}", couponRecordMessage);
            }
            return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS);
        } else {
            throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
        }
    }

    /**
     * 解锁优惠券记录
     * 1）查询task工作单是否存在
     * 2）查询订单状态
     *
     * @param couponRecordMessage
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public boolean releaseCouponRecord(CouponRecordMessage couponRecordMessage) {
        // 查询task工作单是否存在
        CouponTaskDO couponTaskDO = couponTaskMapper.selectOne(new QueryWrapper<CouponTaskDO>().eq("id", couponRecordMessage.getTaskId()));
        if (Objects.isNull(couponTaskDO)) {
            log.warn("工作单不存在，消息：{}", couponRecordMessage);
            return true;
        }
        if (StringUtils.equalsIgnoreCase(couponTaskDO.getLockState(), StockTaskStateEnum.LOCK.name())) {
            // 查询订单状态
            JsonData jsonData = productOrderFeignService.queryProductOrderState(couponRecordMessage.getOutTradeNo());
            if (jsonData.getCode() == 0) {
                // 正常响应，判断订单状态
                String state = jsonData.getData().toString();
                // 返回的数据不存在，返回给消息队列，重新投递
                if (StringUtils.isBlank(state)) {
                    log.warn("订单状态不存在，返回给消息队列，重新投递：{}", couponRecordMessage);
                    return false;
                }

                // 状态是NEW新建状态，返回给消息队列，重新投递
                if (StringUtils.equalsIgnoreCase(ProductOrderStateEnum.NEW.name(), state)) {
                    log.warn("订单状态是NEW，返回给消息队列，重新投递：{}", couponRecordMessage);
                    return false;
                }

                // 状态是已经支付
                if (StringUtils.equalsIgnoreCase(ProductOrderStateEnum.PAY.name(), state)) {
                    // 修改task状态为finish
                    couponTaskDO.setLockState(StockTaskStateEnum.FINISH.name());
                    int rows = couponTaskMapper.updateById(couponTaskDO);
                    if (rows > 0) {
                        log.info("订单已经支付，修改库存锁定工作单状态为FINISH：{}", couponRecordMessage);
                        return true;
                    } else {
                        log.warn("订单已经支付，修改库存锁定工作单状态为FINISH失败，返回给消息队列，重新投递：{}", couponRecordMessage);
                        return false;
                    }
                }
            }
            // 订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复优惠券使用记录为NEW
            log.warn("订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复优惠券使用记录为NEW：{}", couponRecordMessage);
            this.cancelCouponRecord(couponTaskDO);
        } else {
            log.warn("工作单状态不是LOCK，state={}，消息：{}", couponTaskDO.getLockState(), couponRecordMessage);
        }
        return true;
    }

    /**
     * 订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复优惠券使用记录为NEW
     *
     * @param couponTaskDO CouponTaskDO
     */
    @Override
    public void cancelCouponRecord(CouponTaskDO couponTaskDO) {
        couponTaskDO.setLockState(StockTaskStateEnum.CANCEL.name());
        couponTaskMapper.updateById(couponTaskDO);
        // 恢复优惠券记录为NEW状态
        couponRecordMapper.updateState(couponTaskDO.getCouponRecordId(), CouponUseStateEnum.NEW.name());
    }

    /**
     * 订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复优惠券使用记录为NEW
     *
     * @param taskId taskId
     */
    @Override
    public void cancelCouponRecord(Long taskId) {
        CouponTaskDO couponTaskDO = couponTaskMapper.selectOne(new QueryWrapper<CouponTaskDO>().eq("id", taskId));
        this.cancelCouponRecord(couponTaskDO);
    }

    /**
     * 根据ID列表获取优惠券详情并锁定优惠券
     *
     * @param couponDTO CouponDTO
     * @return JsonData
     */
    @Override
    public JsonData queryUserCouponRecord(CouponDTO couponDTO) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        List<Long> couponRecordIdList = couponDTO.getCouponRecordIdList();
        if (Objects.isNull(couponRecordIdList) || couponRecordIdList.size() == 0) {
            return JsonData.returnJson(BizCodeEnum.COUPON_NO_EXITS);
        }
        for (Long id : couponRecordIdList) {
            if (id <= 0) {
                return JsonData.returnJson(BizCodeEnum.COUPON_NO_EXITS);
            }
        }
        List<CouponRecordDO> couponRecordDOList = couponRecordMapper.queryListInIds(couponRecordIdList, loginUser.getId());
        if (Objects.isNull(couponRecordDOList) || couponRecordDOList.size() == 0) {
            return JsonData.returnJson(BizCodeEnum.COUPON_NO_EXITS);
        }
        List<CouponRecordVO> couponRecordVOList = couponRecordDOList.stream().map(obj -> {
            CouponRecordVO couponRecordVO = new CouponRecordVO();
            BeanUtils.copyProperties(obj, couponRecordVO);
            return couponRecordVO;
        }).collect(Collectors.toList());

        return JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS, couponRecordVOList);
    }
}
