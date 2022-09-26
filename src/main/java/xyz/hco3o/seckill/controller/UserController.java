package xyz.hco3o.seckill.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.hco3o.seckill.pojo.User;
import xyz.hco3o.seckill.rabbitmq.MQSender;
import xyz.hco3o.seckill.vo.RespBean;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author hco3o
 * @since 2022-04-13
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MQSender mqSender;

    // 用户信息（用于测试）
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }

    // 测试发送RabbitMQ
    @RequestMapping("/mq")
    @ResponseBody
    public void mq() {
        mqSender.sendFanout("test hello");
    }

    // fanout模式
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void mqFanout01() {
        mqSender.sendFanout("fanout hello");
    }

    // direct模式
    @RequestMapping("/mq/direct01")
    @ResponseBody
    public void mqDirect01() {
        mqSender.sendDirect01("direct hello, red");
    }

    @RequestMapping("/mq/direct02")
    @ResponseBody
    public void mqDirect02() {
        mqSender.sendDirect02("direct hello, green");
    }

    // topic模式
    @RequestMapping("/mq/topic01")
    @ResponseBody
    public void mqTopic01() {
        mqSender.sendTopic01("topic hello, red");
    }

    @RequestMapping("/mq/topic02")
    @ResponseBody
    public void mqTopic02() {
        mqSender.sendTopic02("topic hello, green");
    }
}
