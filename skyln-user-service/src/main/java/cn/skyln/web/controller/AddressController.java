package cn.skyln.web.controller;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.REQ.AddressAddRequest;
import cn.skyln.web.model.VO.AddressVO;
import cn.skyln.web.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
    @PostMapping("/find/{address_id}")
    public JsonData getOneAddress(@ApiParam(value = "地址ID", required = true) @PathVariable("address_id") long addressId) {
        AddressVO addressVO = addressService.getOneById(addressId);
        if (Objects.isNull(addressVO)) {
            return JsonData.returnJson(BizCodeEnum.ADDRESS_NOT_EXIT);
        }
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, addressVO);
    }

    @ApiOperation("新增收获地址")
    @PostMapping("add")
    public JsonData addAddress(@ApiParam(value = "新增收货地址对象", required = true) @RequestBody AddressAddRequest addressAddRequest) {
        return addressService.add(addressAddRequest);
    }

    @ApiOperation("根据ID删除地址")
    @PostMapping("/del/{address_id}")
    public JsonData delAddress(@ApiParam(value = "地址ID", required = true) @PathVariable("address_id") long addressId) {
        return addressService.del(addressId);
    }

    @ApiOperation("查询用户的全部收货地址")
    @PostMapping("/list")
    public JsonData findUserAllAddress() {
        List<AddressVO> list = addressService.listUserAllAddress();
        return JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, list);
    }

}

