package cn.skyln.web.controller;


import cn.skyln.config.AlipayConfig;
import cn.skyln.config.PayUrlConfig;
import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.ClientType;
import cn.skyln.enums.ProductOrderPayTypeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.REQ.ConfirmOrderRequest;
import cn.skyln.web.service.ProductOrderService;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author skylamella
 * @since 2022-09-12
 */
@Api(tags = "订单模块")
@RestController
@RequestMapping("/api/v1/order/")
@Slf4j
public class ProductOrderController {

    @Autowired
    private ProductOrderService productOrderService;

    @Autowired
    private PayUrlConfig payUrlConfig;

    @ApiOperation("用户下单")
    @PostMapping("/confirm")
    public void confirmOrder(@ApiParam(value = "确认订单对象", required = true) @RequestBody ConfirmOrderRequest confirmOrderRequest,
                             HttpServletResponse response) {
        JsonData jsonData = productOrderService.confirmOrder(confirmOrderRequest);
        if (jsonData.getCode() == 0) {
            String client = confirmOrderRequest.getClientType();
            String payType = confirmOrderRequest.getPayType();

            if (StringUtils.equalsIgnoreCase(payType, ProductOrderPayTypeEnum.ALIPAY.name())) {
                // 如果是支付宝网页支付，都是跳转网页，APP除外

                if (StringUtils.equalsIgnoreCase(client, ClientType.APP.name())) {
                    // APP SDK支付，TODO
                    log.info("[创建支付宝APP支付订单成功] {}", confirmOrderRequest);
                } else {
                    log.info("[创建支付宝网页支付订单成功] {}", confirmOrderRequest);
                    writeData(response, jsonData);
                }
            } else if (StringUtils.equalsIgnoreCase(payType, ProductOrderPayTypeEnum.WECHAT.name())) {
                // 如果是微信支付，TODO
                log.info("[创建微信支付订单成功] {}", confirmOrderRequest);
            } else if (StringUtils.equalsIgnoreCase(payType, ProductOrderPayTypeEnum.BANK.name())) {
                // 如果是银行卡支付，TODO
                log.info("[创建银行卡支付订单成功] {}", confirmOrderRequest);
            }
        } else {
            log.error("[创建订单失败] {}", jsonData);
        }
    }

    /**
     * 查询订单状态，此接口没有登录拦截，可以增加一个密钥进行RPC通信
     *
     * @param outTradeNo 订单号
     * @return JsonData
     */
    @ApiOperation("RPC-订单状态查询")
    @GetMapping("query_state")
    public JsonData queryProductOrderState(@ApiParam(value = "订单号", required = true) @RequestParam("out_trade_no") String outTradeNo) {
        String state = productOrderService.queryProductOrderState(outTradeNo);
        return StringUtils.isBlank(state) ? JsonData.returnJson(BizCodeEnum.ORDER_CONFIRM_NOT_EXIST) : JsonData.returnJson(BizCodeEnum.SEARCH_SUCCESS, state);
    }

    private void writeData(HttpServletResponse response, JsonData jsonData) {
        response.setContentType("text/html;charset=UTF8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonData.getData().toString());
            writer.flush();
        } catch (Exception e) {
            log.error("[写出HTML异常] {}", e);
        }
    }

    @GetMapping("test_pay")
    public void testAlipay(HttpServletResponse httpServletResponse) throws AlipayApiException {
        Map<String, String> content = new HashMap<>();
        //商户订单号,64个字符以内、可包含字母、数字、下划线；需保证在商户端不重复
        String no = UUID.randomUUID().toString();
        log.info("订单号:{}", no);
        content.put("out_trade_no", no);
        content.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //订单总金额，单位为元，精确到小数点后两位
        content.put("total_amount", String.valueOf("111.99"));
        //商品标题/交易标题/订单标题/订单关键字等。 注意：不可使用特殊字符，如 /，=，&amp; 等。
        content.put("subject", "杯子");
        //商品描述，可空
        content.put("body", "好的杯子");
        // 该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点， 如 1.5h，可转换为 90m。
        content.put("timeout_express", "5m");
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizContent(JSON.toJSONString(content));
        request.setNotifyUrl(payUrlConfig.getAlipayCallbackUrl());
        request.setReturnUrl(payUrlConfig.getAlipaySuccessReturnUrl());
        AlipayTradeWapPayResponse response = AlipayConfig.getInstance().pageExecute(request);
        if (response.isSuccess()) {
            log.info("支付宝支付调用成功");
            String body = response.getBody();
            httpServletResponse.setContentType("text/html;charset=UTF-8");
            try (PrintWriter writer = httpServletResponse.getWriter()){
                writer.write(body);
                writer.flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            log.info("支付宝支付调用失败");
        }
    }
}

