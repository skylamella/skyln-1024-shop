package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.model.ProductMessage;
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
 * @Date: 2022/09/22/20:45
 * @Description:
 */
@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.stock_release_queue}")
public class StockMQListener {
    @Autowired
    private RedissonClient redissonClient;

    @RabbitHandler
    public void releaseStockRecord(ProductMessage productMessage, Message message, Channel channel) throws IOException {
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, "stock_record_release", productMessage.getTaskId());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：releaseStockRecord消息内容：{}", productMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        boolean flag = false;

    }
}
