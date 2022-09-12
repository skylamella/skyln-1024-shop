package cn.skyln.web.service.impl;

import cn.skyln.web.model.DO.ProductOrderDO;
import cn.skyln.web.mapper.ProductOrderMapper;
import cn.skyln.web.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-09-12
 */
@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderService {

}
