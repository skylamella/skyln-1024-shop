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
            writeData(response, jsonData);
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

