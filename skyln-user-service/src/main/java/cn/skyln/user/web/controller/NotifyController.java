package cn.skyln.user.web.controller;

import cn.skyln.common.enums.BizCodeEnum;
import cn.skyln.common.enums.SendCodeEnum;
import cn.skyln.common.exceptions.BizException;
import cn.skyln.common.utils.CommonUtils;
import cn.skyln.common.utils.JsonData;
import cn.skyln.user.web.service.NotifyService;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lamella
 * @Date: 2022/09/01/22:11
 * @Description:
 */
@Api(tags = "通知模块")
@RestController
@RequestMapping("api/v1/notify")
@Slf4j
public class NotifyController {

    @Autowired
    @Qualifier("captchaProducer")
    private Producer captchaProducer;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 10分钟有效
     */
    private static final long CAPTCHA_CODE_EXPIRED = 10;

    @ApiOperation("获取图形验证码")
    @GetMapping("captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        String captchaText = captchaProducer.createText();
        redisTemplate.opsForValue().set(getCaptchaKey(request), captchaText, CAPTCHA_CODE_EXPIRED, TimeUnit.MINUTES);
        log.info("[图形验证码： {}]", captchaText);
        BufferedImage image = captchaProducer.createImage(captchaText);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            ImageIO.write(image, "jpg", outputStream);
        } catch (Exception e) {
            throw new BizException(e.hashCode(), e.getMessage());
        }
    }

    @ApiOperation("发送注册验证码")
    @PostMapping("send_code")
    public JsonData sendRegisterCode(@ApiParam(value = "收件人", required = true) @RequestParam(value = "to") String to,
                                     @ApiParam(value = "图形验证码", required = true) @RequestParam(value = "captcha") String captcha,
                                     HttpServletRequest request) {
        String key = getCaptchaKey(request);
        String cacheCaptcha = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(cacheCaptcha) || !StringUtils.equalsIgnoreCase(captcha, cacheCaptcha)) {
            return JsonData.returnJson(BizCodeEnum.CODE_CAPTCHA_ERROR);
        } else {
            redisTemplate.delete(key);
            return notifyService.sendCode(SendCodeEnum.USER_REGISTER, to);
        }
    }

    /**
     * 获取缓存的key
     *
     * @param request
     * @return
     */
    private String getCaptchaKey(HttpServletRequest request) {
        return "user-service:captcha:" + CommonUtils.MD5(request.getHeader("User-Agent") + CommonUtils.getIpAddr(request));
    }
}
