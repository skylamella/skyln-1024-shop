package cn.skyln.web.controller;


import cn.skyln.enums.BizCodeEnum;
import cn.skyln.enums.ClientType;
import cn.skyln.enums.ProductOrderPayTypeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.model.REQ.ConfirmOrderRequest;
import cn.skyln.web.service.ProductOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

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
}

