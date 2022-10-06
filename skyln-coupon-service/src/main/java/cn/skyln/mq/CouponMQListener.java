package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.enums.MQChannelStateEnum;
import cn.skyln.model.CouponRecordMessage;
import cn.skyln.utils.CheckUtil;
import cn.skyln.web.service.CouponRecordService;
import cn.skyln.web.service.MqErrorLogService;
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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqErrorLogService mqErrorLogService;

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
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, MQChannelStateEnum.COUPON_RECORD_RELEASE.name(), couponRecordMessage.getTaskId());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：releaseCouponRecord消息内容：{}", couponRecordMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        int retryNums = 1;
        String mqKey = String.format(CacheKey.MQ_KEY, MQChannelStateEnum.MQ_COUPON_RECORD_RELEASE.name(), couponRecordMessage.getOutTradeNo() + ":" + couponRecordMessage.getTaskId());
        try {
            log.info("{}-分布式锁加锁成功:{}", MQChannelStateEnum.COUPON_RECORD_RELEASE.getMsg(), Thread.currentThread().getId());
            if (couponRecordService.releaseCouponRecord(couponRecordMessage)) {
                // 确认消息消费成功
                channel.basicAck(msgTag, false);
            } else {
                retryNums = CheckUtil.checkMQRetryNums(redisTemplate,
                        log,
                        mqKey,
                        couponRecordMessage,
                        msgTag,
                        channel,
                        MQChannelStateEnum.COUPON_RECORD_RELEASE.getMsg());
//                if (redisTemplate.hasKey(mqKey)) {
//                    retryNums = (int) redisTemplate.opsForValue().get(mqKey);
//                    redisTemplate.delete(mqKey);
//                    if (retryNums < 5) {
//                        redisTemplate.opsForValue().set(mqKey, ++retryNums);
//                        log.error("释放优惠券失败，第{}次重试 flag=false：{}", retryNums, couponRecordMessage);
//                        channel.basicReject(msgTag, true);
//                    } else {
//                        log.error("释放优惠券-失败，重试次数超过5次 flag=false：{}", couponRecordMessage);
//                        // 重试次数超过5次，确认消息消费成功
//                        channel.basicAck(msgTag, false);
//                    }
//                } else {
//                    log.error("释放优惠券-失败，第1次重试 flag=false：{}", couponRecordMessage);
//                    redisTemplate.opsForValue().set(mqKey, 1);
//                    channel.basicReject(msgTag, true);
//                }
            }
        } catch (Exception e) {
            log.error("{}-记录异常：{}，msg：{}", MQChannelStateEnum.COUPON_RECORD_RELEASE.getMsg(), e, couponRecordMessage);
            retryNums = Math.max(retryNums, CheckUtil.removeMQRedisKey(redisTemplate, mqKey, msgTag, channel));
            lock.unlock();
        } finally {
            if (retryNums >= 5) {
                // 持续消费失败则插入数据库
                mqErrorLogService.insertMqErrorLog(couponRecordMessage.getOutTradeNo());
                // 订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复优惠券使用记录为NEW
                log.warn("订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复优惠券使用记录为NEW：{}", couponRecordMessage);
                couponRecordService.cancelCouponRecord(couponRecordMessage.getTaskId());
            }
            lock.unlock();
            log.info("{}-分布式锁解锁成功:{}", MQChannelStateEnum.COUPON_RECORD_RELEASE.getMsg(), Thread.currentThread().getId());
        }
    }
}
