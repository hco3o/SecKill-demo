package xyz.hco3o.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.hco3o.seckill.pojo.Order;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hco3o
 * @since 2022-04-14
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
