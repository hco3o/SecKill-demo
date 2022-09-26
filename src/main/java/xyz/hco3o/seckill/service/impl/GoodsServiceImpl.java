package xyz.hco3o.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.hco3o.seckill.mapper.GoodsMapper;
import xyz.hco3o.seckill.pojo.Goods;
import xyz.hco3o.seckill.service.IGoodsService;
import xyz.hco3o.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hco3o
 * @since 2022-04-14
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    // 获取商品列表
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    // 获取商品详情
    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}
