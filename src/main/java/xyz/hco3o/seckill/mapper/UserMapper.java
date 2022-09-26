package xyz.hco3o.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.hco3o.seckill.pojo.User;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hco3o
 * @since 2022-04-13
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
