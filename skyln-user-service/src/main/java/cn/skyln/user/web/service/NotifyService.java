package cn.skyln.user.web.service;

import cn.skyln.common.enums.SendCodeEnum;
import cn.skyln.common.utils.JsonData;

/**
 * @Author: lamella
 * @Date: 2022/09/02/22:23
 * @Description:
 */
public interface NotifyService {

    JsonData sendCode(SendCodeEnum sendCodeEnum, String to);
}
