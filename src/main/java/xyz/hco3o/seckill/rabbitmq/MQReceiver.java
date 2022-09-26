package xyz.hco3o.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

// 消息消费者
@Service
@Slf4j
public class MQReceiver {
    @RabbitListener(queues = "queue")
    public void receiveTest(Object msg) {
        log.info("接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_fanout01")
    public void receiveFanout01(Object msg) {
        log.info("QUEUE01接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_fanout02")
    public void receiveFanout02(Object msg) {
        log.info("QUEUE02接收消息：" + msg);
    }

    // direct模式
    @RabbitListener(queues = "queue_direct01")
    public void receiveDirect01(Object msg) {
        log.info("QUEUE01接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_direct02")
    public void receiveDirect02(Object msg) {
        log.info("QUEUE02接收消息：" + msg);
    }

    // topic模式
    @RabbitListener(queues = "queue_topic01")
    public void receiveTopic01(Object msg) {
        log.info("QUEUE1接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_topic02")
    public void receiveTopic02(Object msg) {
        log.info("QUEUE2接收消息：" + msg);
    }
}
