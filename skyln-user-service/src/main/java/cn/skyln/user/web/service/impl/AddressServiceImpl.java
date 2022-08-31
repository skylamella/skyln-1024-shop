package cn.skyln.user.web.service.impl;

import cn.skyln.user.web.mapper.AddressMapper;
import cn.skyln.user.web.model.DO.AddressDO;
import cn.skyln.user.web.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 电商-公司收发货地址表 服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-08-30
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressDO> implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public AddressDO getOneById(long addressId) {
        return addressMapper.selectOne(new QueryWrapper<AddressDO>().eq("id", addressId));
    }
}
