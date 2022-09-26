package xyz.hco3o.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.hco3o.seckill.pojo.Goods;
import xyz.hco3o.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hco3o
 * @since 2022-04-14
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    // 获取商品列表
    List<GoodsVo> findGoodsVo();

    // 获取商品详情
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
