package xyz.hco3o.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMQ配置类-Fanout模式
@Configuration
public class RabbitMQFanoutConfig {

    private static final String QUEUE01 = "queue_fanout01";
    private static final String QUEUE02 = "queue_fanout02";
    private static final String EXCHANGE = "fanoutExchange";

    // 消息放到队列
    @Bean
    public Queue queueTest() {
        // 名称、消息需不需要持久化
        return new Queue("queue", true);
    }

    @Bean
    public Queue queueFanout01() {
        return new Queue(QUEUE01);
    }

    @Bean
    public Queue queueFanout02() {
        return new Queue(QUEUE02);
    }

    @Bean
    public FanoutExchange fanoutExchange01() {
        return new FanoutExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingFanout01() {
        return BindingBuilder.bind(queueFanout01()).to(fanoutExchange01());
    }

    @Bean
    public Binding bindingFanout02() {
        return BindingBuilder.bind(queueFanout02()).to(fanoutExchange01());
    }
}
