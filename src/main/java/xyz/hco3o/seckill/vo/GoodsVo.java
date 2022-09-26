package xyz.hco3o.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.hco3o.seckill.pojo.Goods;

import java.math.BigDecimal;
import java.util.Date;

// 商品返回对象
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo extends Goods {
    // 只需要把秒杀的一些属性加上就可以了
    private BigDecimal seckillPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
