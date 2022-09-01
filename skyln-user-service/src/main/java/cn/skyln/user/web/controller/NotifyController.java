package cn.skyln.user.web.controller;

import cn.skyln.common.exceptions.BizException;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;

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

    @ApiOperation("获取图形验证码")
    @GetMapping("captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        String captchaText = captchaProducer.createText();
        log.info("[图形验证码： {}]", captchaText);
        BufferedImage image = captchaProducer.createImage(captchaText);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            ImageIO.write(image, "jpg", outputStream);
        } catch (Exception e) {
            throw new BizException(e.hashCode(), e.getMessage());
        }
    }
}
