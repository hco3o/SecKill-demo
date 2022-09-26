package xyz.hco3o.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.hco3o.seckill.pojo.Order;
import xyz.hco3o.seckill.pojo.User;
import xyz.hco3o.seckill.vo.GoodsVo;
import xyz.hco3o.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hco3o
 * @since 2022-04-14
 */
public interface IOrderService extends IService<Order> {

    // 秒杀
    Order seckill(User user, GoodsVo goodsVo);

    // 订单详情
    OrderDetailVo detail(Long orderId);

    // 获取秒杀地址
    String createPath(User user, Long goodsId);

    // 校验秒杀地址
    boolean checkPath(User user, Long goodsId, String path);

    // 校验验证码
    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
