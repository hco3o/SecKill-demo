package xyz.hco3o.seckill.vo;


import lombok.Data;
import org.hibernate.validator.constraints.Length;
import xyz.hco3o.seckill.validator.IsMobile;

import javax.validation.constraints.NotNull;

// 登录参数
@Data
public class LoginVo {
    @NotNull
    @IsMobile
    private String mobile;
    @NotNull
    @Length(min = 32)
    private String password;
}
