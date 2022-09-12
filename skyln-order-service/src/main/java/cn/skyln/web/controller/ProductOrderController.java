package cn.skyln.web.controller;


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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("RPC-新用户注册领券接口")
    @PostMapping("/confirm")
    public void confirmOrder(@ApiParam(value = "确认订单对象", required = true) @RequestBody ConfirmOrderRequest confirmOrderRequest,
                             HttpServletResponse response) {
        JsonData jsonData = productOrderService.confirmOrder(confirmOrderRequest);
        if (jsonData.getCode() == 0) {
            String client = confirmOrderRequest.getClientType();
            String payType = confirmOrderRequest.getPayType();

            if (StringUtils.equalsIgnoreCase(payType, ProductOrderPayTypeEnum.ALIPAY.name())) {
                // 如果是支付宝网页支付，都是跳转网页，APP除外

                if(StringUtils.equalsIgnoreCase(client, ClientType.APP.name())){
                    // APP SDK支付，TODO
                    log.info("[创建支付宝APP支付订单成功] {}", confirmOrderRequest);
                }else{
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

