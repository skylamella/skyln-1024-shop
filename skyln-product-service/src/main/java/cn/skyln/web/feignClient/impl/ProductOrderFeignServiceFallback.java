package cn.skyln.web.feignClient.impl;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.ProductOrderFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: lamella
 * @Date: 2022/09/20/21:54
 * @Description:
 */
@Slf4j
@Service
public class ProductOrderFeignServiceFallback implements ProductOrderFeignService {
    @Override
    public JsonData queryProductOrderState(String outTradeNo) {
        return JsonData.returnJson(BizCodeEnum.SYSTEM_ERROR);
    }
}
