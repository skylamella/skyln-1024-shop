package cn.skyln.web.service.impl;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.mapper.BannerMapper;
import cn.skyln.web.model.DO.BannerDO;
import cn.skyln.web.model.VO.BannerVO;
import cn.skyln.web.service.BannerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-10
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, BannerDO> implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    /**
     * 查询所有轮播图
     *
     * @return JsonData
     */
    @Override
    public JsonData findAllBanner() {
        List<BannerDO> bannerDOList = bannerMapper.selectList(new QueryWrapper<BannerDO>().orderByDesc("weight"));
        if (Objects.isNull(bannerDOList)) {
            return JsonData.returnJson(BizCodeEnum.BANNER_NOT_EXIT);
        }
        List<BannerVO> bannerVOList = bannerDOList.stream().map(obj -> {
            BannerVO bannerVO = new BannerVO();
            BeanUtils.copyProperties(obj, bannerVO);
            return bannerVO;
        }).collect(Collectors.toList());
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, bannerVOList);
    }
}
