package cn.skyln.user.web.service;

import cn.skyln.common.utils.JsonData;
import cn.skyln.user.web.model.DO.AddressDO;
import cn.skyln.user.web.model.REQ.AddressAddRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 电商-公司收发货地址表 服务类
 * </p>
 *
 * @author skylamella
 * @since 2022-08-30
 */
public interface AddressService extends IService<AddressDO> {
    AddressDO getOneById(long addressId);

    JsonData add(AddressAddRequest addressAddRequest);
}
