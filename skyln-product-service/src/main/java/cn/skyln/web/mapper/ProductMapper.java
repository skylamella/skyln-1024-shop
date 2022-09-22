package cn.skyln.web.mapper;

import cn.skyln.web.model.DO.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author skylamella
 * @since 2022-09-10
 */
public interface ProductMapper extends BaseMapper<ProductDO> {

    /**
     * 锁定商品库存
     *
     * @param productId 商品ID
     * @param buyNum    购买数量
     * @return 影响行数
     */
    int lockProductStock(@Param("productId") long productId, @Param("buyNum") int buyNum);

    /**
     * 恢复商品库存
     *
     * @param productId 商品ID
     * @param buyNum    购买数量
     * @return 影响行数
     */
    int unlockProductStock(@Param("productId") Long productId, @Param("buyNum") Integer buyNum);
}
