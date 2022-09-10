package cn.skyln.web.service.impl;

import cn.skyln.web.model.DO.ProductDO;
import cn.skyln.web.mapper.ProductMapper;
import cn.skyln.web.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-10
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

}
