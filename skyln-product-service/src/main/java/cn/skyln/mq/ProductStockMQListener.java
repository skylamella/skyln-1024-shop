package cn.skyln.mq;

import cn.skyln.constant.CacheKey;
import cn.skyln.enums.StockTaskStateEnum;
import cn.skyln.model.ProductStockMessage;
import cn.skyln.mqError.mapper.MqErrorLogMapper;
import cn.skyln.mqError.model.MqErrorLogDO;
import cn.skyln.web.mapper.ProductMapper;
import cn.skyln.web.mapper.ProductTaskMapper;
import cn.skyln.web.model.DO.ProductTaskDO;
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
    private MqErrorLogMapper mqErrorLogMapper;

    @Autowired
    private ProductTaskMapper productTaskMapper;

    @Autowired
    private ProductMapper productMapper;

    @RabbitHandler
    public void releaseProductStockRecord(ProductStockMessage productStockMessage, Message message, Channel channel) throws IOException {
        String lockKey = String.format(CacheKey.DISTRIBUTED_LOCK_KEY, "stock_record_release", productStockMessage.getTaskId());
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        log.info("监听到消息：releaseStockRecord消息内容：{}", productStockMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();
        int retryNums = 1;
        try {
            log.info("释放库存-分布式锁加锁成功:{}", Thread.currentThread().getId());
            if (productService.releaseProductStockRecord(productStockMessage)) {
                // 确认消息消费成功
                channel.basicAck(msgTag, false);
            } else {
                String mqKey = String.format(CacheKey.MQ_KEY, "mq_stock_record_release", productStockMessage.getOutTradeNo() + ":" + productStockMessage.getTaskId());
                if (redisTemplate.hasKey(mqKey)) {
                    retryNums = (int) redisTemplate.opsForValue().get(mqKey);
                    redisTemplate.delete(mqKey);
                    if (retryNums < 5) {
                        redisTemplate.opsForValue().set(mqKey, ++retryNums);
                        log.error("释放商品库存-失败，第{}次重试 flag=false：{}", retryNums, productStockMessage);
                        channel.basicReject(msgTag, true);
                    } else {
                        log.error("释放商品库存-失败，重试次数超过5次 flag=false：{}", productStockMessage);
                        // 重试次数超过5次，确认消息消费成功
                        channel.basicAck(msgTag, false);
                    }
                } else {
                    log.error("释放商品库存-失败，第1次重试 flag=false：{}", productStockMessage);
                    redisTemplate.opsForValue().set(mqKey, 1);
                    channel.basicReject(msgTag, true);
                }
            }
        } catch (Exception e) {
            log.error("释放商品库存-记录异常：{}，msg：{}", e, productStockMessage);
            channel.basicReject(msgTag, true);
            lock.unlock();
        } finally {
            if (retryNums >= 5) {
                // 持续消费失败则插入数据库
                MqErrorLogDO mqErrorLogDO = new MqErrorLogDO();
                mqErrorLogDO.setOutTradeNo(productStockMessage.getOutTradeNo());
                mqErrorLogMapper.insert(mqErrorLogDO);

                ProductTaskDO productTaskDO = productTaskMapper.selectById(productStockMessage.getTaskId());
                log.warn("订单不存在，或者订单被取消，确认消息，修改task状态为CANCEL，恢复商品库存：{}", productStockMessage);
                productTaskDO.setLockState(StockTaskStateEnum.CANCEL.name());
                productTaskMapper.updateById(productTaskDO);
                // 恢复商品库存
                productMapper.unlockProductStock(productTaskDO.getProductId(), productTaskDO.getBuyNum());
            }
            lock.unlock();
            log.info("释放库存-分布式锁解锁成功:{}", Thread.currentThread().getId());
        }
    }
}
