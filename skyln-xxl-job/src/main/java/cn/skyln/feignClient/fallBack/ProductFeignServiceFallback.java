package cn.skyln.feignClient.fallBack;

import cn.skyln.feignClient.ProductFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: lamella
 * @Date: 2022/10/23/18:49
 * @Description:
 */
@Slf4j
@Service
public class ProductFeignServiceFallback implements ProductFeignService {
    @Override
    public int redisCart2MysqlCart() {
        return 0;
    }
}
