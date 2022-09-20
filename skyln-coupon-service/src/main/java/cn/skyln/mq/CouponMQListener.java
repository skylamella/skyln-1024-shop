package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.model.CouponRecordMessage;
import cn.skyln.web.service.CouponRecordService;
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
 * @Date: 2022/09/20/21:23
 * @Description:
 */
@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.coupon_release_queue}")
public class CouponMQListener {

    @Autowired
    private CouponRecordService couponRecordService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 重复消费-幂等性
     * <p>
     * 消费失败，重新入队最大重试次数：
     * 如果消费失败，不重新入队，记录日志，插入到数据库后人工排查
     *
     * @param couponRecordMessage
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void releaseCouponRecord(CouponRecordMessage couponRecordMessage, Message message, Channel channel) throws IOException {
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, "coupon_record_release", couponRecordMessage.getTaskId());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：releaseCouponRecord消息内容：{}", couponRecordMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        boolean flag = false;
        try {
            log.info("释放优惠券分布式锁加锁成功:{}", Thread.currentThread().getId());
            flag = couponRecordService.releaseCouponRecord(couponRecordMessage);
            if (flag) {
                // 确认消息消费成功
                channel.basicAck(msgTag, false);
            } else {
                log.error("释放优惠券失败 flag=false：{}", couponRecordMessage);
                channel.basicReject(msgTag, true);
            }
        } catch (Exception e) {
            log.error("释放优惠券记录异常：{}，msg：{}", e, couponRecordMessage);
            channel.basicReject(msgTag, true);
        } finally {
            if(!flag){
                // todo 持续消费失败则插入数据库
            }
            lock.unlock();
            log.info("释放优惠券分布式锁解锁成功:{}", Thread.currentThread().getId());
        }
    }
}
