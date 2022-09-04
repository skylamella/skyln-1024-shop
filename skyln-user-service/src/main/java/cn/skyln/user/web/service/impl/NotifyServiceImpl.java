package cn.skyln.user.web.service.impl;

import cn.skyln.common.constant.CacheKey;
import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.enums.SendCodeEnum;
import cn.skyln.common.utils.CheckUtil;
import cn.skyln.common.utils.CommonUtils;
import cn.skyln.common.utils.JsonData;
import cn.skyln.user.component.MailComponent;
import cn.skyln.user.web.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author: lamella
 * @Date: 2022/09/02/22:31
 * @Description:
 */
@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private MailComponent mailComponent;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String SUBJECT = "skyln1024商城验证码";
    private static final String CONTENT = "欢迎\"%s\"注册skyln1024商城，您的验证码是\"%s\"，有效期10分钟，请勿向他人透漏验证码。";
    /**
     * 10分钟有效
     */
    private static final long CAPTCHA_CODE_EXPIRED = 10;

    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        String cacheKey = String.format(CacheKey.CHECK_CODE_KEY, sendCodeEnum.name(), to);
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);

        // 如果redis中存在缓存，则判断是否为60秒内重复发送
        if (StringUtils.isNotBlank(cacheValue)) {
            long ttl = Long.parseLong(cacheValue.split("_")[1]);
            // 当前时间戳减去验证码发送时间戳，如果小于60秒，则判定为重复发送
            long opsRepeatSendTimeStamp = CommonUtils.getCurrentTimeStamp() - ttl;
            if (opsRepeatSendTimeStamp < (60 * 1000)) {
                log.error("重复发送验证码，时间间隔：{}秒", opsRepeatSendTimeStamp / 1000);
                return JsonData.returnJson(BizCodeEnum.CODE_LIMITED);
            }
        }
        String code = CommonUtils.getRandomCode(6);
        String value = code + "_" + CommonUtils.getCurrentTimeStamp();
        redisTemplate.opsForValue().set(cacheKey, value, CAPTCHA_CODE_EXPIRED, TimeUnit.MINUTES);
        if (CheckUtil.isEmail(to)) {
            mailComponent.sendSimpleMail(to, SUBJECT, String.format(CONTENT, to, code));
            log.info("{}的验证码发送成功！", to);
            return JsonData.returnJson(BizCodeEnum.SEND_CODE_SUCCESS, code);
        } else if (CheckUtil.isPhone(to)) {
            log.info("{}的验证码发送成功！", to);
            return JsonData.returnJson(BizCodeEnum.SEND_CODE_SUCCESS, code);
        }
        log.error("{}的号码输入不合规！", to);
        return JsonData.returnJson(BizCodeEnum.CODE_TO_ERROR);
    }

    @Override
    public boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code) {
        String cacheKey = String.format(CacheKey.CHECK_CODE_KEY, sendCodeEnum.name(), to);
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            return StringUtils.equals(cacheValue.split("_")[0], code);
        }
        return false;
    }
}
