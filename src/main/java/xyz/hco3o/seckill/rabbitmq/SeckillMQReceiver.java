package xyz.hco3o.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xyz.hco3o.seckill.pojo.SeckillMessage;
import xyz.hco3o.seckill.pojo.SeckillOrder;
import xyz.hco3o.seckill.pojo.User;
import xyz.hco3o.seckill.service.IGoodsService;
import xyz.hco3o.seckill.service.IOrderService;
import xyz.hco3o.seckill.utils.JsonUtil;
import xyz.hco3o.seckill.vo.GoodsVo;

@Service
@Slf4j
public class SeckillMQReceiver {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

    // 下单
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message) {
        log.info("接收到的消息：" + message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();
        // 获取商品对象判断库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        // 判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return;
        }
        // 下单
        orderService.seckill(user, goodsVo);
    }
}
