package cn.skyln.web.mapper;

import cn.skyln.web.model.DO.ProductOrderItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author skylamella
 * @since 2022-09-12
 */
public interface ProductOrderItemMapper extends BaseMapper<ProductOrderItemDO> {

    /**
     * 批量插入订单项
     *
     * @param orderItemList 订单项列表
     * @return 影响行数
     */
    int insertBatch(@Param("orderItemList") List<ProductOrderItemDO> orderItemList);
}
