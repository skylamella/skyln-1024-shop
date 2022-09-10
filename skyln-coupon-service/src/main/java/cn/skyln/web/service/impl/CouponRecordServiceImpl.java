package cn.skyln.web.service.impl;

import cn.skyln.interceptor.LoginInterceptor;
import cn.skyln.model.LoginUser;
import cn.skyln.utils.CouponUtils;
import cn.skyln.web.mapper.CouponRecordMapper;
import cn.skyln.web.model.DO.CouponRecordDO;
import cn.skyln.web.model.VO.CouponRecordVO;
import cn.skyln.web.service.CouponRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
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
        return CouponUtils.getReturnPageMap(recordDOPage.getTotal(),
                recordDOPage.getPages(),
                recordDOPage.getRecords().stream().map(obj ->
                                CouponUtils.beanProcess(obj, new CouponRecordVO()))
                        .collect(Collectors.toList()));
    }
}
