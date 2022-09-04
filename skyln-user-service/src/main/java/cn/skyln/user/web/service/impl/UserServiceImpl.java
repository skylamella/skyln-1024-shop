package cn.skyln.user.web.service.impl;

import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.enums.SendCodeEnum;
import cn.skyln.common.model.LoginUser;
import cn.skyln.common.utils.CommonUtils;
import cn.skyln.common.utils.JWTUtils;
import cn.skyln.common.utils.JsonData;
import cn.skyln.common.utils.RsaUtils;
import cn.skyln.user.web.mapper.UserMapper;
import cn.skyln.user.web.model.DO.UserDO;
import cn.skyln.user.web.model.REQ.UserLoginRequest;
import cn.skyln.user.web.model.REQ.UserRegisterRequest;
import cn.skyln.user.web.service.NotifyService;
import cn.skyln.user.web.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author skylamella
 * @since 2022-08-30
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 24小时有效
     */
    private static final long CODE_EXPIRED = 24;

    @Override
    public UserDO getOneByMail(String mail) {
        return userMapper.selectOne(new QueryWrapper<UserDO>().eq("mail", mail));
    }

    /**
     * 用户注册
     * 邮箱验证码验证
     * 密码加密
     * 账号唯一性检查
     * 插入数据库
     * 新注册用户福利发放(TODO)
     *
     * @param userRegisterRequest 用户注册请求对象
     * @return JsonData
     */
    @Override
    public JsonData userRegister(UserRegisterRequest userRegisterRequest) {
        // 判断邮箱是否传入
        if (StringUtils.isEmpty(userRegisterRequest.getMail())) {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_NOT_EXIST_ERROR.getCode(),
                    BizCodeEnum.ACCOUNT_NOT_EXIST_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_NOT_EXIST_ERROR);
        }
        // 判断传递的密码是否为空
        if (StringUtils.isEmpty(userRegisterRequest.getPwd()) || StringUtils.isEmpty(userRegisterRequest.getRePwd())) {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_PWD_NOT_EXIST_ERROR.getCode(),
                    BizCodeEnum.ACCOUNT_PWD_NOT_EXIST_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_PWD_NOT_EXIST_ERROR);
        }
        // 判断输入的密码和确认密码是否相同
        if (!StringUtils.equals(userRegisterRequest.getPwd(), userRegisterRequest.getRePwd())) {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_REGISTER_PWD_ERROR.getCode(),
                    BizCodeEnum.ACCOUNT_REGISTER_PWD_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_REGISTER_PWD_ERROR);
        }
        // 判断验证码是否正确
        if (!notifyService.checkCode(SendCodeEnum.USER_REGISTER, userRegisterRequest.getMail(), userRegisterRequest.getCode())) {
            log.error("[{}] {}",
                    BizCodeEnum.CODE_ERROR.getCode(),
                    BizCodeEnum.CODE_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.CODE_ERROR);
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userRegisterRequest, userDO);
        userDO.setCreateTime(new Date());
        // 密码加盐
        userDO.setSecret("$1$" + CommonUtils.getRandomCode(8));
        // 密码加密
        userDO.setPwd(Md5Crypt.md5Crypt(userRegisterRequest.getPwd().getBytes(), userDO.getSecret()));
        // 邮箱唯一性校验
        if (checkUnique(userDO.getMail())) {
            int insert = userMapper.insert(userDO);
            if (insert == 1) {
                // TODO 新用户注册福利发放
                userRegisterInitTask(userDO);
                log.info("[{}] \"{}\"{}",
                        BizCodeEnum.ACCOUNT_REGISTER_SUCCESS.getCode(),
                        userRegisterRequest.getMail(),
                        BizCodeEnum.ACCOUNT_REGISTER_SUCCESS.getMsg());
                return JsonData.returnJson(BizCodeEnum.ACCOUNT_REGISTER_SUCCESS);
            } else {
                log.error("[{}] {}",
                        BizCodeEnum.ACCOUNT_REGISTER_ERROR.getCode(),
                        BizCodeEnum.ACCOUNT_REGISTER_ERROR.getMsg());
                return JsonData.returnJson(BizCodeEnum.ACCOUNT_REGISTER_ERROR);
            }
        } else {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_REPEAT.getCode(),
                    BizCodeEnum.ACCOUNT_REPEAT.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_REPEAT);
        }
    }

    @Override
    public void userUpdate(UserDO userDO) {
        userMapper.updateById(userDO);
    }

    @Override
    public JsonData userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 判断传递的密码是否为空
        if (StringUtils.isEmpty(userLoginRequest.getMail())) {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_NOT_EXIST_ERROR.getCode(),
                    BizCodeEnum.ACCOUNT_NOT_EXIST_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_NOT_EXIST_ERROR);
        }
        // 判断传递的密码是否为空
        if (StringUtils.isEmpty(userLoginRequest.getPwd())) {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_PWD_NOT_EXIST_ERROR.getCode(),
                    BizCodeEnum.ACCOUNT_PWD_NOT_EXIST_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_PWD_NOT_EXIST_ERROR);
        }
        // 判断账号是否存在
        List<UserDO> list = userMapper.selectList(new QueryWrapper<UserDO>().eq("mail", userLoginRequest.getMail()));
        if (list == null || list.size() <= 0) {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_LOGIN_ERROR.getCode(),
                    BizCodeEnum.ACCOUNT_LOGIN_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_LOGIN_ERROR);
        }
        UserDO userDO = list.get(0);
        // 判断密码是否正确
        String md5Crypt = Md5Crypt.md5Crypt(userLoginRequest.getPwd().getBytes(), userDO.getSecret());
        if (!StringUtils.equals(userDO.getPwd(), md5Crypt)) {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_LOGIN_ERROR.getCode(),
                    BizCodeEnum.ACCOUNT_LOGIN_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_LOGIN_ERROR);
        }
        // 登录成功，生成JWT token
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(userDO, loginUser);
        try {
            String token;
            String refreshToken;
            // 1：开启安全模式
            if (userLoginRequest.getSafeMode() == 1) {
                token = JWTUtils.generateToken(loginUser, RsaUtils.getPrivateKey(), CommonUtils.getIpAddr(request));
                refreshToken = JWTUtils.generateRefreshToken(loginUser, RsaUtils.getPrivateKey(), CommonUtils.getIpAddr(request));
            } else {
                token = JWTUtils.generateToken(loginUser, RsaUtils.getPrivateKey());
                refreshToken = JWTUtils.generateRefreshToken(loginUser, RsaUtils.getPrivateKey());
            }
            redisTemplate.opsForValue().set(token, refreshToken, CODE_EXPIRED, TimeUnit.HOURS);
            log.info("[{}] \"{}\"{}",
                    BizCodeEnum.LOGIN_SUCCESS.getCode(),
                    userLoginRequest.getMail(),
                    BizCodeEnum.LOGIN_SUCCESS.getMsg());
            return JsonData.returnJson(BizCodeEnum.LOGIN_SUCCESS, token);
        } catch (Exception e) {
            log.error("[{}] {}",
                    BizCodeEnum.ACCOUNT_LOGIN_ERROR.getCode(),
                    BizCodeEnum.ACCOUNT_LOGIN_ERROR.getMsg());
            return JsonData.returnJson(BizCodeEnum.ACCOUNT_LOGIN_ERROR);
        }
    }

    private boolean checkUnique(String mail) {
        return userMapper.selectList(new QueryWrapper<UserDO>().eq("mail", mail)).size() <= 0;
    }

    private void userRegisterInitTask(UserDO userDO) {

    }
}
