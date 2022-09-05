package cn.skyln.user.web.service.impl;

import cn.skyln.common.enums.AddressStatusEnum;
import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.interceptor.LoginInterceptor;
import cn.skyln.common.model.LoginUser;
import cn.skyln.common.utils.JsonData;
import cn.skyln.user.web.mapper.AddressMapper;
import cn.skyln.user.web.model.DO.AddressDO;
import cn.skyln.user.web.model.REQ.AddressAddRequest;
import cn.skyln.user.web.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

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

    @Override
    public JsonData add(AddressAddRequest addressAddRequest) {
        AddressDO addressDO = new AddressDO();
        BeanUtils.copyProperties(addressAddRequest, addressDO);
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        addressDO.setUserId(loginUser.getId());
        addressDO.setCreateTime(new Date());
        // 判断是否有默认收货地址
        if (addressDO.getDefaultStatus() == AddressStatusEnum.DEFAULT_ADDRESS.getStatus()) {
            AddressDO defaultAddressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>()
                    .eq("user_id", loginUser.getId())
                    .eq("default_status", AddressStatusEnum.DEFAULT_ADDRESS.getStatus()));
            if (Objects.nonNull(defaultAddressDO)) {
                defaultAddressDO.setDefaultStatus(AddressStatusEnum.COMMON_ADDRESS.getStatus());
                addressMapper.update(defaultAddressDO, new QueryWrapper<AddressDO>()
                        .eq("id", defaultAddressDO.getId()));
            }
        }
        int insert = addressMapper.insert(addressDO);
        return insert == 1 ? JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS) : JsonData.returnJson(BizCodeEnum.SYSTEM_ERROR);
    }
}
