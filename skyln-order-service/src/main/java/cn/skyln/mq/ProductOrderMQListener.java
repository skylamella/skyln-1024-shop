package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.model.OrderCloseMessage;
import cn.skyln.web.service.ProductOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author: lamella
 * @Date: 2022/09/24/23:11
 * @Description:
 */
@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.order_close_queue}")
public class ProductOrderMQListener {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ProductOrderService productOrderService;

    @RabbitHandler
    public void delayCloseProductOrder(OrderCloseMessage orderCloseMessage, Message message, Channel channel) throws IOException {
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, "delay_order_close", orderCloseMessage.getOutTradeNo());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：delayOrderClose消息内容：{}", orderCloseMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        boolean flag = false;
        try {
            log.info("延迟自动关单-分布式锁加锁成功:{}", Thread.currentThread().getId());
            flag = productOrderService.delayCloseProductOrder(orderCloseMessage);
            if (flag) {
                // 确认消息消费成功
                channel.basicAck(msgTag, false);
            } else {
                log.error("延迟自动关单-失败 flag=false：{}", orderCloseMessage);
                channel.basicReject(msgTag, true);
            }
        } catch (Exception e) {
            log.error("延迟自动关单-记录异常：{}，msg：{}", e, orderCloseMessage);
            channel.basicReject(msgTag, true);
            lock.unlock();
        } finally {
            if (!flag) {
                // todo 持续消费失败则插入数据库
            }
            lock.unlock();
            log.info("延迟自动关单-分布式锁解锁成功:{}", Thread.currentThread().getId());
        }
    }
}
