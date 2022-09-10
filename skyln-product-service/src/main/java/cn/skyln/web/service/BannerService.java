package cn.skyln.web.service;

import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DO.BannerDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-10
 */
public interface BannerService extends IService<BannerDO> {

    /**
     * 查询所有轮播图
     *
     * @return JsonData
     */
    JsonData findAllBanner();
}
