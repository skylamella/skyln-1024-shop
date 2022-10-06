package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.enums.MQChannelStateEnum;
import cn.skyln.model.ProductStockMessage;
import cn.skyln.utils.CheckUtil;
import cn.skyln.web.service.MqErrorLogService;
import cn.skyln.web.service.ProductService;
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
 * @Date: 2022/09/22/20:45
 * @Description:
 */
@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.stock_release_queue}")
public class ProductStockMQListener {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqErrorLogService mqErrorLogService;

    @RabbitHandler
    public void releaseProductStockRecord(ProductStockMessage productStockMessage, Message message, Channel channel) throws IOException {
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, MQChannelStateEnum.STOCK_RECORD_RELEASE.name(), productStockMessage.getTaskId());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：releaseStockRecord消息内容：{}", productStockMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        int retryNums = 1;
        String mqKey = String.format(CacheKey.MQ_KEY, MQChannelStateEnum.MQ_STOCK_RECORD_RELEASE.name(), productStockMessage.getOutTradeNo() + ":" + productStockMessage.getTaskId());
        try {
            log.info("{}-分布式锁加锁成功:{}", MQChannelStateEnum.STOCK_RECORD_RELEASE.getMsg(), Thread.currentThread().getId());
            if (productService.releaseProductStockRecord(productStockMessage)) {
                // 确认消息消费成功
                channel.basicAck(msgTag, false);
            } else {
                retryNums = CheckUtil.checkMQRetryNums(redisTemplate,
                        log,
                        mqKey,
                        productStockMessage,
                        msgTag,
                        channel,
                        MQChannelStateEnum.STOCK_RECORD_RELEASE.getMsg());
//                if (redisTemplate.hasKey(mqKey)) {
//                    retryNums = (int) redisTemplate.opsForValue().get(mqKey);
//                    redisTemplate.delete(mqKey);
//                    if (retryNums < 5) {
//                        redisTemplate.opsForValue().set(mqKey, ++retryNums);
//                        log.error("释放商品库存-失败，第{}次重试 flag=false：{}", retryNums, productStockMessage);
//                        channel.basicReject(msgTag, true);
//                    } else {
//                        log.error("释放商品库存-失败，重试次数超过5次 flag=false：{}", productStockMessage);
//                        // 重试次数超过5次，确认消息消费成功
//                        channel.basicAck(msgTag, false);
//                    }
//                } else {
//                    log.error("释放商品库存-失败，第1次重试 flag=false：{}", productStockMessage);
//                    redisTemplate.opsForValue().set(mqKey, 1);
//                    channel.basicReject(msgTag, true);
//                }
            }
        } catch (Exception e) {
            log.error("{}-记录异常：{}，msg：{}", MQChannelStateEnum.STOCK_RECORD_RELEASE.getMsg(), e, productStockMessage);
            retryNums = Math.max(retryNums, CheckUtil.removeMQRedisKey(redisTemplate, mqKey, msgTag, channel));
        } finally {
            if (retryNums >= 5) {
                // 持续消费失败则插入数据库
                mqErrorLogService.insertMqErrorLog(productStockMessage.getOutTradeNo());
                log.warn("订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复商品库存：{}", productStockMessage);
                productService.cancelProductStockRecord(productStockMessage.getTaskId());
            }
            lock.unlock();
            log.info("{}-分布式锁解锁成功:{}", MQChannelStateEnum.STOCK_RECORD_RELEASE.getMsg(), Thread.currentThread().getId());
        }
    }
}
