package cn.skyln.enums;

import lombok.Getter;

/**
 * @Author: lamella
 * @Date: 2022/09/05/22:52
 * @Description:
 */
public enum AddressStatusEnum {

    /*
        是默认收货地址
     */
    DEFAULT_ADDRESS(1),
    /*
        非默认收货地址
     */
    COMMON_ADDRESS(0);

    @Getter
    private int status;

    private AddressStatusEnum(int status) {
        this.status = status;
    }

}
