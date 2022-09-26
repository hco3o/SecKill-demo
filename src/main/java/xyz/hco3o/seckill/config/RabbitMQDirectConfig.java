package xyz.hco3o.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMQ配置类-Direct模式
@Configuration
public class RabbitMQDirectConfig {
    private static final String QUEUE01 = "queue_direct01";
    private static final String QUEUE02 = "queue_direct02";
    private static final String EXCHANGE = "directExchange";
    private static final String ROUTING_KEY01 = "queue.red";
    private static final String ROUTING_KEY02 = "queue.green";

    @Bean
    public Queue queueDirect01() {
        return new Queue(QUEUE01);
    }

    @Bean
    public Queue queueDirect02() {
        return new Queue(QUEUE02);
    }

    @Bean
    public DirectExchange directExchange01() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingDirect01() {
        return BindingBuilder.bind(queueDirect01()).to(directExchange01()).with(ROUTING_KEY01);
    }

    @Bean
    public Binding bindingDirect02() {
        return BindingBuilder.bind(queueDirect02()).to(directExchange01()).with(ROUTING_KEY02);
    }
}
