package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.enums.MQChannelStateEnum;
import cn.skyln.model.OrderCloseMessage;
import cn.skyln.utils.CheckUtil;
import cn.skyln.web.service.MqErrorLogService;
import cn.skyln.web.service.ProductOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqErrorLogService mqErrorLogService;

    @RabbitHandler
    public void delayCloseProductOrder(OrderCloseMessage orderCloseMessage, Message message, Channel channel) throws IOException {
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, MQChannelStateEnum.DELAY_ORDER_CLOSE.name(), orderCloseMessage.getOutTradeNo());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：delayOrderClose消息内容：{}", orderCloseMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        int retryNums = 0;
        String mqKey = String.format(CacheKey.MQ_KEY, MQChannelStateEnum.MQ_DELAY_ORDER_CLOSE.name(), orderCloseMessage.getOutTradeNo());
        try {
            log.info("{}-分布式锁加锁成功:{}", MQChannelStateEnum.DELAY_ORDER_CLOSE.getMsg(), Thread.currentThread().getId());
            if (productOrderService.delayCloseProductOrder(orderCloseMessage)) {
                // 确认消息消费成功
                channel.basicAck(msgTag, false);
            } else {
                retryNums = CheckUtil.checkMQRetryNums(redisTemplate,
                        log,
                        mqKey,
                        orderCloseMessage,
                        msgTag,
                        channel,
                        MQChannelStateEnum.DELAY_ORDER_CLOSE.getMsg());
//                if (redisTemplate.hasKey(mqKey)) {
//                    retryNums = (int) redisTemplate.opsForValue().get(mqKey);
//                    redisTemplate.delete(mqKey);
//                    if (retryNums < 5) {
//                        redisTemplate.opsForValue().set(mqKey, ++retryNums);
//                        log.error("延迟自动关单-失败，第{}次重试 flag=false：{}", retryNums, orderCloseMessage);
//                        channel.basicReject(msgTag, true);
//                    } else {
//                        log.error("延迟自动关单-失败，重试次数超过5次 flag=false：{}", orderCloseMessage);
//                        // 重试次数超过5次，确认消息消费成功
//                        channel.basicAck(msgTag, false);
//                    }
//                } else {
//                    log.error("延迟自动关单-失败，第1次重试 flag=false：{}", orderCloseMessage);
//                    redisTemplate.opsForValue().set(mqKey, 0);
//                    channel.basicReject(msgTag, true);
//                }
            }
        } catch (Exception e) {
            log.error("{}-记录异常：{}，msg：{}", MQChannelStateEnum.DELAY_ORDER_CLOSE.getMsg(), e, orderCloseMessage);
            retryNums = Math.max(retryNums, CheckUtil.removeMQRedisKey(redisTemplate, mqKey, msgTag, channel));
            lock.unlock();
        } finally {
            if (retryNums >= 5) {
                // 持续消费失败则插入数据库
                mqErrorLogService.insertMqErrorLog(orderCloseMessage.getOutTradeNo());
                // 本地取消订单
                log.info("结果为空，则未支付成功，本地取消订单：{}", orderCloseMessage);
                productOrderService.cancelCloseProductOrder(orderCloseMessage.getOutTradeNo());
            }
            lock.unlock();
            log.info("{}-分布式锁解锁成功:{}", MQChannelStateEnum.DELAY_ORDER_CLOSE.getMsg(), Thread.currentThread().getId());
        }
    }
}
