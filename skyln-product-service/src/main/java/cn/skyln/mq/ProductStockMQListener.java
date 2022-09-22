package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.model.ProductStockMessage;
import cn.skyln.web.service.ProductService;
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
public class ProductStockMQListener {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ProductService productService;

    @RabbitHandler
    public void releaseProductStockRecord(ProductStockMessage productStockMessage, Message message, Channel channel) throws IOException {
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, "stock_record_release", productStockMessage.getTaskId());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：releaseStockRecord消息内容：{}", productStockMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        boolean flag = false;
        try {
            log.info("释放库存分布式锁加锁成功:{}", Thread.currentThread().getId());
            flag = productService.releaseProductStockRecord(productStockMessage);
            if (flag) {
                // 确认消息消费成功
                channel.basicAck(msgTag, false);
            } else {
                log.error("释放商品库存失败 flag=false：{}", productStockMessage);
                channel.basicReject(msgTag, true);
            }
        } catch (Exception e) {
            log.error("释放商品库存记录异常：{}，msg：{}", e, productStockMessage);
            channel.basicReject(msgTag, true);
        } finally {
            if (!flag) {
                // todo 持续消费失败则插入数据库
            }
            lock.unlock();
            log.info("释放库存分布式锁解锁成功:{}", Thread.currentThread().getId());
        }
    }
}
