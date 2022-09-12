package cn.skyln.web.service.impl;

import cn.skyln.utils.CommonUtils;
import cn.skyln.web.mapper.ProductMapper;
import cn.skyln.web.model.DO.ProductDO;
import cn.skyln.web.model.VO.ProductDetailVO;
import cn.skyln.web.model.VO.ProductListVO;
import cn.skyln.web.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 分页查询商品
     *
     * @param page 第几页
     * @param size 一页显示几条
     * @return Map
     */
    @Override
    public Map<String, Object> pageProductActivity(int page, int size) {
        Page<ProductDO> pageInfo = new Page<>(page, size);
        IPage<ProductDO> productDOIPage = productMapper.selectPage(pageInfo, null);
        return CommonUtils.getReturnPageMap(productDOIPage.getTotal(),
                productDOIPage.getPages(),
                productDOIPage.getRecords().stream().map(obj ->
                                CommonUtils.beanProcess(obj, new ProductListVO()))
                        .collect(Collectors.toList()));
    }

    /**
     * 根据ID查询商品详情
     *
     * @param productId 商品ID
     * @return ProductDetailVO
     */
    @Override
    public ProductDetailVO findDetailById(long productId) {
        ProductDO productDO = productMapper.selectById(productId);
        return beanProcess(productDO);
    }

    /**
     * 根据ID批量查询商品
     *
     * @param productIdList ID列表
     * @return 商品列表
     */
    @Override
    public List<ProductDetailVO> findProductsByIdBatch(List<Long> productIdList) {
        List<ProductDO> productDOList = productMapper.selectList(new QueryWrapper<ProductDO>().in("id", productIdList));
        return productDOList.stream().map(this::beanProcess).collect(Collectors.toList());
    }

    private ProductDetailVO beanProcess(ProductDO productDO) {
        ProductDetailVO productDetailVO = new ProductDetailVO();
        BeanUtils.copyProperties(productDO, productDetailVO);
        productDetailVO.setStock(productDO.getStock() - productDO.getLockStock());
        return productDetailVO;
    }
}
