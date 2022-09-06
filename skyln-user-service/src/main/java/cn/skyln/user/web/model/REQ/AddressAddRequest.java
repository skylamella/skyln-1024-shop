package cn.skyln.user.web.model.REQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: lamella
 * @Date: 2022/09/05/22:27
 * @Description:
 */
@Data
@ApiModel(value = "新增收货地址对象", description = "新增收货地址对象")
public class AddressAddRequest {
    /**
     * 是否默认收货地址：0->否；1->是
     */
    @JsonProperty("default_status")
    @ApiModelProperty(value = "是否是默认收货地址", example = "0")
    private Integer defaultStatus;

    /**
     * 收发货人姓名
     */
    @JsonProperty("receive_name")
    @ApiModelProperty(value = "收货人姓名", example = "凉月")
    private String receiveName;

    /**
     * 收货人电话
     */
    @ApiModelProperty(value = "手机号", example = "13100000000")
    private String phone;

    /**
     * 省/直辖市
     */
    @ApiModelProperty(value = "省份", example = "北京")
    private String province;

    /**
     * 市
     */
    @ApiModelProperty(value = "城市", example = "北京")
    private String city;

    /**
     * 区
     */
    @ApiModelProperty(value = "区", example = "朝阳区")
    private String region;

    /**
     * 详细地址
     */
    @JsonProperty("detail_address")
    @ApiModelProperty(value = "详细地址", example = "虚拟物品专用收货地址")
    private String detailAddress;
}
