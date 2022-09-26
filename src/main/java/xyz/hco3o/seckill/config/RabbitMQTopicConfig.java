package xyz.hco3o.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMQ配置类-Topic
@Configuration
public class RabbitMQTopicConfig {
    private static final String QUEUE01 = "queue_topic01";
    private static final String QUEUE02 = "queue_topic02";
    private static final String EXCHANGE = "topicExchange";
    private static final String ROUTING_KEY01 = "#.queue.#";
    private static final String ROUTING_KEY02 = "*.queue.#";

    @Bean
    public Queue queueTopic01() {
        return new Queue(QUEUE01);
    }

    @Bean
    public Queue queueTopic02() {
        return new Queue(QUEUE02);
    }

    @Bean
    public TopicExchange topicExchange01() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingTopic01() {
        return BindingBuilder.bind(queueTopic01()).to(topicExchange01()).with(ROUTING_KEY01);
    }

    @Bean
    public Binding bindingTopic02() {
        return BindingBuilder.bind(queueTopic02()).to(topicExchange01()).with(ROUTING_KEY02);
    }
}
