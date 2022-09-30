package cn.skyln.components.strategy.impl;

import cn.skyln.components.strategy.PayStrategy;
import cn.skyln.config.AlipayConfig;
import cn.skyln.config.PayUrlConfig;
import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.ClientType;
import cn.skyln.exception.BizException;
import cn.skyln.web.model.VO.PayInfoVO;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: lamella
 * @Date: 2022/09/27/22:20
 * @Description:
 */
@Service
@Slf4j
public class AlipayStrategy implements PayStrategy {

    @Autowired
    private PayUrlConfig payUrlConfig;

    @Override
    public String unifyOrder(PayInfoVO payInfoVO) {
        Map<String, String> content = new HashMap<>();
        String outTradeNo = payInfoVO.getOutTradeNo();
        log.info("订单号:{}", outTradeNo);
        content.put("out_trade_no", outTradeNo);
        content.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //订单总金额，单位为元，精确到小数点后两位
        content.put("total_amount", payInfoVO.getPayAmount().toString());
        //商品标题/交易标题/订单标题/订单关键字等。 注意：不可使用特殊字符，如 /，=，&amp; 等。
        content.put("subject", payInfoVO.getTitle());
        //商品描述，可空
        content.put("body", payInfoVO.getDescription());
        // 该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点， 如 1.5h，可转换为 90m。
        double timeOut = Math.floor(payInfoVO.getOrderPayTimeMills() / (1000 * 60));
        // 前端也需要判断订单是否要关闭了，如果快要到期则不给二次支付
        if (timeOut < 1) {
            throw new BizException(BizCodeEnum.PAY_ORDER_PAY_TIMEOUT);
        }
        content.put("timeout_express", Double.valueOf(timeOut) + "m");
        return payWithType(payInfoVO, content);
    }

    private String payWithType(PayInfoVO payInfoVO, Map<String, String> content) {
        String payType = payInfoVO.getPayType();
        String form = "";
        try {
            if (StringUtils.equalsIgnoreCase(payType, ClientType.H5.name())) {
                // H5手机网页支付
                AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
                request.setBizContent(JSON.toJSONString(content));
                request.setNotifyUrl(payUrlConfig.getAlipayCallbackUrl());
                request.setReturnUrl(payUrlConfig.getAlipaySuccessReturnUrl());
                AlipayTradeWapPayResponse response = AlipayConfig.getInstance().pageExecute(request);
                if (response.isSuccess()) {
                    log.info("支付宝H5手机网页支付构建表单成功：response={}，payInfoVO={}", response, payInfoVO);
                    form = response.getBody();
                } else {
                    log.error("支付宝H5手机网页支付构建表单失败：response={}，payInfoVO={}", response, payInfoVO);
                }
            } else if (StringUtils.equalsIgnoreCase(payType, ClientType.PC.name())) {
                // PC网页支付
                AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
                // 该参数数值不接受小数点， 如 1.5h，可转换为 90m。
                double timeOut = Math.floor(payInfoVO.getOrderPayTimeMills() / (1000 * 60));
                // 前端也需要判断订单是否要关闭了，如果快要到期则不给二次支付
                if (timeOut < 1) {
                    throw new BizException(BizCodeEnum.PAY_ORDER_PAY_TIMEOUT);
                }
                content.remove("timeout_express");
                content.put("time_expire", Double.valueOf(timeOut) + "m");
//                content.put("passback_params", "");
//                content.put("extend_params", JSON.toJSONString(new HashMap<String, String>().put("sys_service_provider_id", "")));
                request.setBizContent(JSON.toJSONString(content));
                request.setNotifyUrl(payUrlConfig.getAlipayCallbackUrl());
                request.setReturnUrl(payUrlConfig.getAlipaySuccessReturnUrl());
                AlipayTradePagePayResponse response = AlipayConfig.getInstance().pageExecute(request);
                if (response.isSuccess()) {
                    log.info("支付宝PC网页支付构建表单成功：response={}，payInfoVO={}", response, payInfoVO);
                    form = response.getBody();
                } else {
                    log.error("支付宝PC网页支付构建表单失败：response={}，payInfoVO={}", response, payInfoVO);
                }
            }
        } catch (AlipayApiException e) {
            log.error("支付宝构建表单异常：payInfoVO={}，异常={}", payInfoVO, e);
        }
        return form;
    }

    @Override
    public String refund(PayInfoVO payInfoVO) {
        return PayStrategy.super.refund(payInfoVO);
    }

    @Override
    public String queryPaySuccess(PayInfoVO payInfoVO) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, String> content = new HashMap<>();
        content.put("out_trade_no", payInfoVO.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(content));
        AlipayTradeQueryResponse response = null;
        try {
            response = AlipayConfig.getInstance().execute(request);
            log.info("支付宝订单查询响应：{}", response);
        } catch (AlipayApiException e) {
            log.info("支付宝订单查询异常：{}", e);
        }
        if (Objects.nonNull(response)) {
            if (response.isSuccess()) {
                log.info("支付宝订单状态查询成功：{}", payInfoVO);
                return response.getTradeStatus();
            }
        }
        log.info("支付宝订单状态查询失败：{}", payInfoVO);
        return "";
    }
}
