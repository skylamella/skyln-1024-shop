package cn.skyln.biz;

import cn.skyln.ProductApplication;
import cn.skyln.config.RabbitMQConfig;
import cn.skyln.model.CouponRecordMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: lamella
 * @Date: 2022/09/20/21:08
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProductApplication.class)
@Slf4j
public class MQTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void send() {
        rabbitTemplate.convertAndSend("stock.event.exchange", "stock.release.delay.routing.key", "5qeqweqw");
    }

    @Test
    public void testCouponRecordRelease(){
        CouponRecordMessage couponRecordMessage = new CouponRecordMessage();
        couponRecordMessage.setOutTradeNo("123456abc");
        couponRecordMessage.setTaskId(1L);
        rabbitTemplate.convertAndSend("stock.event.exchange", "stock.release.delay.routing.key", couponRecordMessage);
    }
}
