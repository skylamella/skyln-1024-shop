package cn.skyln.user.web.controller;


import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.utils.JsonData;
import cn.skyln.user.web.model.DO.AddressDO;
import cn.skyln.user.web.model.REQ.AddressAddRequest;
import cn.skyln.user.web.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 电商-公司收发货地址表 前端控制器
 * </p>
 *
 * @author skylamella
 * @since 2022-08-30
 */
@Api(tags = "收货地址模块")
@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @ApiOperation("根据ID查找地址详情")
    @RequestMapping("/find/{address_id}")
    public JsonData getOneAddress(@ApiParam(value = "地址ID", required = true) @PathVariable long address_id) {
        AddressDO addressDO = addressService.getOneById(address_id);
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, addressDO);
    }

    @ApiOperation("新增收获地址")
    @RequestMapping("add")
    public JsonData addAddress(@ApiParam(value = "收货地址对象", required = true) @RequestBody AddressAddRequest addressAddRequest){
        return addressService.add(addressAddRequest);
    }

}

