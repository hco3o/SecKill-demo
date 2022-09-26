package xyz.hco3o.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.hco3o.seckill.pojo.Goods;
import xyz.hco3o.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hco3o
 * @since 2022-04-14
 */
public interface IGoodsService extends IService<Goods> {
    // 获取商品列表
    List<GoodsVo> findGoodsVo();

    // 根据商品ID获取商品详情
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
