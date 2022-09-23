package cn.skyln.web.feignClient.fallBack;

import cn.skyln.enums.BizCodeEnum;
import cn.skyln.utils.JsonData;
import cn.skyln.web.feignClient.ProductFeignService;
import cn.skyln.web.model.DTO.CartDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: lamella
 * @Date: 2022/09/23/21:44
 * @Description:
 */
@Slf4j
@Service
public class ProductFeignServiceFallback implements ProductFeignService {
    @Override
    public JsonData confirmOrderCartItem(CartDTO cartDTO) {
        return JsonData.returnJson(BizCodeEnum.SYSTEM_ERROR);
    }
}
