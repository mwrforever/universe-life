package com.universe.life.auth.resource.util;

import cn.hutool.core.util.StrUtil;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author 毛伟然
 * @since 2025/12/3 17:22
 */
@RequiredArgsConstructor
public class VerifyCaptchaUtil {

    private final StringRedisTemplate stringRedisTemplate;


    public boolean verifyCaptchaIssuer(CaptchaUsageType usage, String identification, String issuer) {
        // 先在redis中查看当前验证的业务标识
        String key = RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX +
                usage.getDisplayName() + ":" +
                identification + ":" +
                RedisConstants.AUTH_ISSUER;
        return checkCode(issuer, key);
    }

    private boolean checkCode(String issuer, String key) {
        String rawIssuer = stringRedisTemplate.opsForValue().get(key);
        // 如果验证码已过期，则返回错误
        if (StrUtil.isBlank(rawIssuer)) {
            return false;
        }
        // 校验授权码与当前业务标识的验证码是否一致
        if (!rawIssuer.equals(issuer)) {
            return false;
        }
        // 删除issuer标识
        stringRedisTemplate.delete(key);
        return true;
    }

    public boolean verifyCaptcha(CaptchaUsageType usage, String identification, String captcha) {
        // 从redis中获取验证码
        String key = RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX +
                usage.getDisplayName() + ":" +
                identification + ":" +
                RedisConstants.AUTH_IDENTIFICATION;
        return checkCode(captcha, key);
    }

}
