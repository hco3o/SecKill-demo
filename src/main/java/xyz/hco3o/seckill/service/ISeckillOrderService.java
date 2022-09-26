package xyz.hco3o.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.hco3o.seckill.pojo.SeckillOrder;
import xyz.hco3o.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hco3o
 * @since 2022-04-14
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    // 获取秒杀结果：orderId表示成功、-1表示失败、0表示排队中
    Long getResult(User user, Long goodsId);
}
