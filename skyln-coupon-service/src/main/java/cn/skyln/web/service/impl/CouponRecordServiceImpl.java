package cn.skyln.web.service.impl;

import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.LoginUser;
import cn.skyln.utils.CommonUtils;
import cn.skyln.web.mapper.CouponRecordMapper;
import cn.skyln.web.model.DO.CouponRecordDO;
import cn.skyln.web.model.VO.CouponRecordVO;
import cn.skyln.web.service.CouponRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class CouponRecordServiceImpl extends ServiceImpl<CouponRecordMapper, CouponRecordDO> implements CouponRecordService {

    @Autowired
    private CouponRecordMapper couponRecordMapper;

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
}
