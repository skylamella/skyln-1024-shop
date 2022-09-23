package cn.skyln.web.service;

import cn.skyln.model.ProductStockMessage;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.DO.ProductDO;
import cn.skyln.web.model.REQ.LockProductRequest;
import cn.skyln.web.model.VO.ProductDetailVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-10
 */
public interface ProductService extends IService<ProductDO> {

    /**
     * 分页查询商品
     *
     * @param page 第几页
     * @param size 一页显示几条
     * @return Map
     */
    Map<String, Object> pageProductActivity(int page, int size);

    /**
     * 根据ID查询商品详情
     *
     * @param productId 商品ID
     * @return ProductDetailVO
     */
    ProductDetailVO findDetailById(long productId);

    /**
     * 根据ID批量查询商品
     *
     * @param productIdList ID列表
     * @return 商品列表
     */
    List<ProductDetailVO> findProductsByIdBatch(List<Long> productIdList);

    /**
     * 锁定商品库存
     *
     * @param lockProductRequest 商品锁定对象
     * @return JsonData
     */
    JsonData lockProductStock(LockProductRequest lockProductRequest);

    /**
     * 释放商品库存
     *
     * @param productStockMessage MQ消息体
     * @return 清理结果
     */
    boolean releaseProductStockRecord(ProductStockMessage productStockMessage);
}
