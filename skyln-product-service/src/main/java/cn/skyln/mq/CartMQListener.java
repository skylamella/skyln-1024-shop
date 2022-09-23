package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.model.CartMessage;
import cn.skyln.web.service.CartService;
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
 * @Date: 2022/09/23/22:20
 * @Description:
 */
@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.cart_release_queue}")
public class CartMQListener {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CartService cartService;

    @RabbitHandler
    public void cleanCartRecord(CartMessage cartMessage, Message message, Channel channel) throws IOException {
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, "clean_cart_record", cartMessage.getOutTradeNo() + cartMessage.getProductId());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：cleanCartRecord消息内容：{}", cartMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        boolean flag = false;
        try {
            log.info("清空购物车-分布式锁加锁成功:{}", Thread.currentThread().getId());
            flag = cartService.cleanCartRecord(cartMessage);
            if (flag) {
                // 确认消息消费成功
                channel.basicAck(msgTag, false);
            } else {
                log.error("释放商品库存失败 flag=false：{}", cartMessage);
                channel.basicReject(msgTag, true);
            }
        } catch (Exception e) {
            log.error("清空购物车-记录异常：{}，msg：{}", e, cartMessage);
            channel.basicReject(msgTag, true);
            lock.unlock();
        } finally {
            if (!flag) {
                // todo 持续消费失败则插入数据库
            }
            lock.unlock();
            log.info("清空购物车-分布式锁解锁成功:{}", Thread.currentThread().getId());
        }
    }
}
