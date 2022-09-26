package xyz.hco3o.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import xyz.hco3o.seckill.pojo.User;
import xyz.hco3o.seckill.service.IGoodsService;
import xyz.hco3o.seckill.service.IUserService;
import xyz.hco3o.seckill.vo.DetailVo;
import xyz.hco3o.seckill.vo.GoodsVo;
import xyz.hco3o.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

// 商品
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    // 做手动渲染
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    // 跳转到商品列表页
    // Windows优化前QPS: 1140.2
    // Linux优化前: 1311.0
    // Windows缓存QPS: 3132.8
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {

        // 在Redis里获取页面，如果不为空直接返回
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        // 把用户对象传到前端页面
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());

        // Redis中查不到页面，手动渲染
        // 渲染的时候需要两个参数：模板名称（goodsList）、IContext（这里使用WebContext）
        // WebContext构造函数需要入参：request、response（直接获取）、servletContext、locale（通过request获取）
        //                           map（想要放到Thymeleaf里的数据，作用相当于model）
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (!StringUtils.isEmpty(html)) {
            // 放到缓存里，并且加上失效时间
            // 因为扣了库存后，页面中有些内容后会变化
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }

        // 加了ResponseBody，直接返回一个html字符串
        // return "goodsList";
        return html;
    }

    // 跳转商品详情页
    @RequestMapping(value = "/toDetail2/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model, User user, @PathVariable Long goodsId, HttpServletRequest request,
                           HttpServletResponse response) {


        ValueOperations valueOperations = redisTemplate.opsForValue();

        // 先从Redis中根据goodsId读取页面
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);

        // 处理时间，传给前端一个状态码
        // 还有一个倒计时（如果秒杀未开始的话）
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        int seckillStatus = 0;
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            // 秒杀未开始
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {
            // 秒杀已结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            // 正在秒杀
            seckillStatus = 1;
        }
        model.addAttribute("secKillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        // return "goodsDetail";
        return html;
    }


    // 跳转商品详情页
    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(User user, @PathVariable Long goodsId) {

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 处理时间，传给前端一个状态码
        // 还有一个倒计时（如果秒杀未开始的话）
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        int seckillStatus = 0;
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            // 秒杀未开始
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {
            // 秒杀已结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            // 正在秒杀
            seckillStatus = 1;
        }

        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(seckillStatus);
        detailVo.setRemainSeconds(remainSeconds);

        return RespBean.success(detailVo);
    }
}
