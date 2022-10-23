package cn.skyln.web.mapper;

import cn.skyln.web.model.DO.CartItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author skylamella
 * @since 2022-10-23
 */
public interface CartItemMapper extends BaseMapper<CartItemDO> {

    /**
     * 批量插入购物项
     *
     * @param cartItemList 购物项列表
     */
    void insertBatch(@Param("cartItemList") List<CartItemDO> cartItemList);

}
