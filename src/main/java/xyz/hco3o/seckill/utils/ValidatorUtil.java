package xyz.hco3o.seckill.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

// 手机号码校验
public class ValidatorUtil {
    private static final Pattern mobile_pattern = Pattern.compile("^1(3[0-9]|5[0-3,5-9]|7[1-3,5-8]|8[0-9])\\d{8}$");

    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }
        return mobile_pattern.matcher(mobile).matches();
    }
}
