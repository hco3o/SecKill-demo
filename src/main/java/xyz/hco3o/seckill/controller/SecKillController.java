package xyz.hco3o.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.hco3o.seckill.config.AccessLimit;
import xyz.hco3o.seckill.exception.GlobalException;
import xyz.hco3o.seckill.pojo.Order;
import xyz.hco3o.seckill.pojo.SeckillMessage;
import xyz.hco3o.seckill.pojo.SeckillOrder;
import xyz.hco3o.seckill.pojo.User;
import xyz.hco3o.seckill.rabbitmq.SeckillMQSender;
import xyz.hco3o.seckill.service.IGoodsService;
import xyz.hco3o.seckill.service.IOrderService;
import xyz.hco3o.seckill.service.ISeckillOrderService;
import xyz.hco3o.seckill.utils.JsonUtil;
import xyz.hco3o.seckill.vo.GoodsVo;
import xyz.hco3o.seckill.vo.RespBean;
import xyz.hco3o.seckill.vo.RespBeanEnum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// 秒杀
@Slf4j
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillMQSender mqSender;
    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> emptyStockMap = new HashMap<>();

    // 秒杀
    // Windows优化前QPS: 833.8
    // Linux优化前QPS: 1145.0
    @RequestMapping("/doSeckill2")
    public String doSeckill2(Model model, User user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        // 查库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0) {
            // 库存没了，跳到错误页面
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        // 查订单，看用户有没有重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";
        }
        // 放心秒杀
        Order order = orderService.seckill(user, goodsVo);
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);
        return "orderDetail";
    }


    // 秒杀
    // Windows优化前QPS: 833.8
    // Linux优化前QPS: 1145.0
    // Windows缓存后QPS: 1755.8
    // Windows优化接口后QPS: 3673.0
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }


        /**
         * 未使用Redis预减库存
         */
        /*
        // 查库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0) {
            // 库存没了，跳到错误页面
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 查订单，看用户有没有重复抢购
        // SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        // 使用Redis查而不是查数据库
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            // 在Redis中查到了订单，不能重复秒杀
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        // 放心秒杀
        Order order = orderService.seckill(user, goodsVo);
        return RespBean.success(order);
         */

        /**
         * 使用Redis预减库存
         */
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 判断path是不是有效的
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 判断是否重复抢购（与优化前是一样的）
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        // 先判断库存是否为空，如果为空就不用去Redis里查了
        if (emptyStockMap.get(goodsId)) {
            // 库存已空
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 预减库存（原子性操作）
        // 这里获取到的库存是递减之后的库存
        // Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        // 使用LUA脚本减库存
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            emptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sentSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);
    }

    // 初始化时执行的方法，这个时候把商品库存加载到Redis
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            emptyStockMap.put(goodsVo.getId(), false);
        });
    }

    /**
     * 获取秒杀结果
     *
     * @return orderId表示成功、-1表示失败、0表示排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    // 获取秒杀地址
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        /**
         * 下面这段代码使用通用拦截器AccessLimit替代
         */
        // ValueOperations valueOperations = redisTemplate.opsForValue();
        // 限制访问次数，5s内访问5次
        // 从request中获得请求地址
        // String uri = request.getRequestURI();
        // Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
        // if (count == null) {
        //     // 第一次访问，设置失效时间为5s
        //     valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
        // } else if (count < 5) {
        //     // 访问次数小于5
        //     valueOperations.increment(uri + ":" + user.getId());
        // } else {
        //     return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
        // }

        // 检查captcha
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 模板
        response.setContentType("image/img");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 设置算数验证码，放到Redis里（参数设置长宽高）
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }
}
