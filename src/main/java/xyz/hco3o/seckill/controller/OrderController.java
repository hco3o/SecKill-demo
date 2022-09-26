package xyz.hco3o.seckill.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.hco3o.seckill.pojo.User;
import xyz.hco3o.seckill.service.IOrderService;
import xyz.hco3o.seckill.vo.OrderDetailVo;
import xyz.hco3o.seckill.vo.RespBean;
import xyz.hco3o.seckill.vo.RespBeanEnum;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hco3o
 * @since 2022-04-14
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    // 订单详情
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        // 通过ID查询
        OrderDetailVo detail = orderService.detail(orderId);
        return RespBean.success(detail);
    }
}
