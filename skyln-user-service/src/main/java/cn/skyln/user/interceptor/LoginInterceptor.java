package cn.skyln.user.interceptor;

import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.model.LoginUser;
import cn.skyln.common.utils.CommonUtils;
import cn.skyln.common.utils.JWTUtils;
import cn.skyln.common.utils.JsonData;
import cn.skyln.common.utils.RsaUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lamella
 * @Date: 2022/09/04/20:36
 * @Description:
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static ThreadLocal<LoginUser> threadLocal = new InheritableThreadLocal<>();

    /**
     * 24小时有效
     */
    private static final long CODE_EXPIRED = 24;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 读取token
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter("token");
        }

        // 判定token是否为空
        if (StringUtils.isEmpty(token)) {
            CommonUtils.renderJson(response, JsonData.returnJson(BizCodeEnum.ACCOUNT_UNLOGIN_ERROR));
            return false;
        }

        // JWT解密
        Claims claims = JWTUtils.checkJWTPublicKey(token, RsaUtils.getPublicKey());
        // 判断token是否过期
        if (Objects.isNull(claims)) {
            // token过期，从redis中取refreshtoken
            String s = redisTemplate.opsForValue().get(token);
            // 判断redis中的refreshtoken是否存在
            if (StringUtils.isNotBlank(s)) {
                claims = JWTUtils.checkJWTPublicKey(s, RsaUtils.getPublicKey());
                // 判断refreshtoken是否过期
                if (Objects.isNull(claims)) {
                    CommonUtils.renderJson(response, JsonData.returnJson(BizCodeEnum.ACCOUNT_UNLOGIN_ERROR));
                    return false;
                }
                // 安全模式IP校验
                if (!checkIp(claims, request)) {
                    CommonUtils.renderJson(response, JsonData.returnJson(BizCodeEnum.ACCOUNT_SAFE_MODE_RELOGIN));
                    return false;
                }
                // 删除redis中的refreshtoken记录，写入新的记录
                redisTemplate.delete(token);
                token = JWTUtils.refreshGenerateToken(claims, RsaUtils.getPrivateKey());
                redisTemplate.opsForValue().set(token, s, CODE_EXPIRED, TimeUnit.HOURS);
                CommonUtils.renderJson(response, JsonData.returnJson(BizCodeEnum.ACCOUNT_ACCESS_TOKEN_EXPIRED, token));
                return false;
            }
            CommonUtils.renderJson(response, JsonData.returnJson(BizCodeEnum.ACCOUNT_UNLOGIN_ERROR));
            return false;
        }
        if (!checkIp(claims, request)) {
            redisTemplate.delete(token);
            CommonUtils.renderJson(response, JsonData.returnJson(BizCodeEnum.ACCOUNT_SAFE_MODE_RELOGIN));
            return false;
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setHeadImg(claims.get("head_img").toString());
        loginUser.setMail(claims.get("mail").toString());
        loginUser.setName(claims.get("name").toString());
        loginUser.setId(Long.valueOf(claims.get("mail").toString()));
        threadLocal.set(loginUser);
        return true;
    }

    /**
     * 安全模式IP校验
     *
     * @param claims  JWT token载荷
     * @param request HttpServletRequest
     * @return 未开启安全模式返回true，否则校验token中的IP是否与当前登录IP相同
     */
    private boolean checkIp(Claims claims, HttpServletRequest request) {
        if (Objects.nonNull(claims.get("ip"))) {
            return StringUtils.equals(CommonUtils.getIpAddr(request), String.valueOf(claims.get("ip")));
        }
        return true;
    }
}
