package cn.skyln.user.web.service.impl;

import cn.skyln.common.enums.AddressStatusEnum;
import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.interceptor.LoginInterceptor;
import cn.skyln.common.model.LoginUser;
import cn.skyln.common.utils.JsonData;
import cn.skyln.user.web.mapper.AddressMapper;
import cn.skyln.user.web.model.DO.AddressDO;
import cn.skyln.user.web.model.REQ.AddressAddRequest;
import cn.skyln.user.web.model.VO.AddressVO;
import cn.skyln.user.web.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * 根据ID查询收货地址
     *
     * @param addressId 收货地址ID
     * @return AddressVO
     */
    @Override
    public AddressVO getOneById(long addressId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        AddressDO addressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>().eq("id", addressId).eq("user_id", loginUser.getId()));
        if (Objects.isNull(addressDO)) {
            return null;
        }
        AddressVO addressVO = new AddressVO();
        BeanUtils.copyProperties(addressDO, addressVO);
        return addressVO;
    }

    /**
     * 新增收货地址
     *
     * @param addressAddRequest 新增收货地址对象
     * @return JsonData
     */
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
                int rows = addressMapper.update(defaultAddressDO, new QueryWrapper<AddressDO>()
                        .eq("id", defaultAddressDO.getId()));
                AddressVO addressVO = new AddressVO();
                BeanUtils.copyProperties(defaultAddressDO, addressVO);
                if (rows != 1) {
                    return JsonData.returnJson(BizCodeEnum.ADDRESS_UPD_FAIL, addressVO);
                }
            }
        }
        int rows = addressMapper.insert(addressDO);
        return rows == 1 ? JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS) : JsonData.returnJson(BizCodeEnum.ADDRESS_ADD_FAIL);
    }

    /**
     * 根据ID删除收货地址
     *
     * @param addressId 收货地址ID
     * @return JsonData
     */
    @Override
    public JsonData del(long addressId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        AddressDO addressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>().eq("id", addressId).eq("user_id", loginUser.getId()));
        if (Objects.isNull(addressDO)) {
            return JsonData.returnJson(BizCodeEnum.ADDRESS_NOT_EXIT);
        }
        int rows = addressMapper.delete(new QueryWrapper<AddressDO>().eq("id", addressId));
        return rows == 1 ? JsonData.returnJson(BizCodeEnum.OPERATE_SUCCESS) : JsonData.returnJson(BizCodeEnum.ADDRESS_DEL_FAIL);
    }

    /**
     * 查询用户所有收货地址
     *
     * @return 用户所有收货地址
     */
    @Override
    public List<AddressVO> listUserAllAddress() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        List<AddressDO> list = addressMapper.selectList(new QueryWrapper<AddressDO>().eq("user_id", loginUser.getId()));
        return list.stream().map(obj -> {
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(obj, addressVO);
            return addressVO;
        }).collect(Collectors.toList());
    }
}
