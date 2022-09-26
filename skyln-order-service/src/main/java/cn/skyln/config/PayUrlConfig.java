package cn.skyln.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: lamella
 * @Date: 2022/09/26/22:00
 * @Description:
 */
@Configuration
@Data
public class PayUrlConfig {

    /**
     * 支付成功页面跳转
     */
    @Value("${myalipay.success_return_url}")
    private String alipaySuccessReturnUrl;

    /**
     * 支付成功回调通知
     */
    @Value("${myalipay.callback_url}")
    private String alipayCallbackUrl;
}
