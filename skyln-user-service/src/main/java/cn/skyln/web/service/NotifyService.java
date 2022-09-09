package cn.skyln.web.service;

import cn.skyln.enums.SendCodeEnum;
import cn.skyln.utils.JsonData;

/**
 * @Author: lamella
 * @Date: 2022/09/02/22:23
 * @Description:
 */
public interface NotifyService {

    JsonData sendCode(SendCodeEnum sendCodeEnum, String to);

    boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code);
}
