package cn.skyln.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: lamella
 * @Date: 2022/09/20/20:43
 * @Description:
 */
@Configuration
@Data
public class RabbitMQConfig {

    /**
     * stock交换机
     */
    @Value("${mqconfig.stock_event_exchange}")
    private String stockEventExchange;
    /**
     * stock第一个队列延迟队列，
     */
    @Value("${mqconfig.stock_release_delay_queue}")
    private String stockReleaseDelayQueue;
    /**
     * stock第一个队列的路由key
     * 进入队列的路由key
     */
    @Value("${mqconfig.stock_release_delay_routing_key}")
    private String stockReleaseDelayRoutingKey;
    /**
     * stock第二个队列，被监听恢复库存的队列
     */
    @Value("${mqconfig.stock_release_queue}")
    private String stockReleaseQueue;
    /**
     * stock第二个队列的路由key
     * <p>
     * 即进入死信队列的路由key
     */
    @Value("${mqconfig.stock_release_routing_key}")
    private String stockReleaseRoutingKey;

    /**
     * cart交换机
     */
    @Value("${mqconfig.cart_event_exchange}")
    private String cartEventExchange;
    /**
     * cart第一个队列延迟队列，
     */
    @Value("${mqconfig.cart_release_delay_queue}")
    private String cartReleaseDelayQueue;
    /**
     * cart第一个队列的路由key
     * 进入队列的路由key
     */
    @Value("${mqconfig.cart_release_delay_routing_key}")
    private String cartReleaseDelayRoutingKey;
    /**
     * cart第二个队列，被监听恢复库存的队列
     */
    @Value("${mqconfig.cart_release_queue}")
    private String cartReleaseQueue;
    /**
     * cart第二个队列的路由key
     * <p>
     * 即进入死信队列的路由key
     */
    @Value("${mqconfig.cart_release_routing_key}")
    private String cartReleaseRoutingKey;

    /**
     * 过期时间
     */
    @Value("${mqconfig.ttl}")
    private Integer ttl;

    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 创建交换机 Topic类型，也可以用Direct路由
     * 一般一个微服务一个交换机
     */
    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange(stockEventExchange, true, false);
    }

    /**
     * 创建延迟队列
     */
    @Bean
    public Queue stockReleaseDelayQueue() {
        Map<String, Object> args = setQueueArgs(stockReleaseRoutingKey, stockEventExchange);
        return new Queue(stockReleaseDelayQueue, true, false, false, args);
    }

    /**
     * 死信队列，普通队列，用于被监听
     */
    @Bean
    public Queue stockReleaseQueue() {
        return new Queue(stockReleaseQueue, true, false, false);
    }

    /**
     * 死信队列绑定关系建立
     */
    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(stockReleaseQueue, Binding.DestinationType.QUEUE, stockEventExchange, stockReleaseRoutingKey, null);
    }

    /**
     * 延迟队列绑定关系建立
     */
    @Bean
    public Binding stockReleaseDelayBinding() {
        return new Binding(stockReleaseDelayQueue, Binding.DestinationType.QUEUE, stockEventExchange, stockReleaseDelayRoutingKey, null);
    }

    /**
     * 创建交换机 Topic类型，也可以用Direct路由
     * 一般一个微服务一个交换机
     */
    @Bean
    public Exchange cartEventExchange() {
        return new TopicExchange(cartEventExchange, true, false);
    }

    /**
     * 创建延迟队列
     */
    @Bean
    public Queue cartReleaseDelayQueue() {
        Map<String, Object> args = setQueueArgs(cartReleaseRoutingKey, cartEventExchange);
        return new Queue(cartReleaseDelayQueue, true, false, false, args);
    }

    /**
     * 死信队列，普通队列，用于被监听
     */
    @Bean
    public Queue cartReleaseQueue() {
        return new Queue(cartReleaseQueue, true, false, false);
    }

    /**
     * 死信队列绑定关系建立
     */
    @Bean
    public Binding cartReleaseBinding() {
        return new Binding(cartReleaseQueue, Binding.DestinationType.QUEUE, cartEventExchange, cartReleaseRoutingKey, null);
    }

    /**
     * 延迟队列绑定关系建立
     */
    @Bean
    public Binding cartReleaseDelayBinding() {
        return new Binding(cartReleaseDelayQueue, Binding.DestinationType.QUEUE, cartEventExchange, cartReleaseDelayRoutingKey, null);
    }

    private Map<String, Object> setQueueArgs(String routingKey, String exchange) {
        Map<String, Object> args = new HashMap<>(3);
        args.put("x-message-ttl", ttl);
        args.put("x-dead-letter-routing-key", routingKey);
        args.put("x-dead-letter-exchange", exchange);
        return args;
    }
}
