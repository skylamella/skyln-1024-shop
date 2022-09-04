package cn.skyln.user.web.service;

import cn.skyln.common.utils.JsonData;
import cn.skyln.user.web.model.DO.UserDO;
import cn.skyln.user.web.model.REQ.UserLoginRequest;
import cn.skyln.user.web.model.REQ.UserRegisterRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author skylamella
 * @since 2022-08-30
 */
public interface UserService extends IService<UserDO> {
    UserDO getOneByMail(String mail);

    JsonData userRegister(UserRegisterRequest userRegisterRequest);

    void userUpdate(UserDO userDO);

    JsonData userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);
}
