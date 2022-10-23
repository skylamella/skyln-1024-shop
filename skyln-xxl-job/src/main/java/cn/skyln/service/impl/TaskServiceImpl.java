package cn.skyln.service.impl;

import cn.skyln.feignClient.ProductFeignService;
import cn.skyln.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: lamella
 * @Date: 2022/10/23/18:44
 * @Description:
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public int redisCart2MysqlCart() {
        return productFeignService.redisCart2MysqlCart();
    }
}
