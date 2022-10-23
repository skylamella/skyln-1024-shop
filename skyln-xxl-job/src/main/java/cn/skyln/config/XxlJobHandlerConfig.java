package cn.skyln.config;

import cn.skyln.constant.XxlJobTaskCalConstant;
import cn.skyln.service.TaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: lamella
 * @Date: 2022/10/23/18:40
 * @Description:
 */
@Component
@Slf4j
public class XxlJobHandlerConfig {
    @Autowired
    private TaskService taskService;

    @XxlJob(XxlJobTaskCalConstant.CART_REDIS_TO_MYSQL)
    public ReturnT<String> redisCart2MysqlCart() throws Exception {
        log.info("[XXL-JOB-{}] Start.", XxlJobTaskCalConstant.CART_REDIS_TO_MYSQL);
        int i = taskService.redisCart2MysqlCart();
        log.info("[XXL-JOB-{}] End.", XxlJobTaskCalConstant.CART_REDIS_TO_MYSQL);
        if(i == 0){
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }
}
