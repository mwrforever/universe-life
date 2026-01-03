package com.universe.life.common.server.api.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.exception.SystemException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.common.server.api.service.ICaptchaService;
import com.universe.life.common.server.api.strategy.CaptchaSenderStrategy;
import com.universe.life.common.server.model.domain.domain.dto.request.CaptchaRequest;
import com.universe.life.common.server.model.domain.domain.dto.request.VerifyCodeFormRequest;
import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import com.universe.life.common.server.model.domain.domain.vo.CaptchaVO;
import com.universe.life.common.util.RedisScriptExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements ICaptchaService {

    private static final String SEND_CAPTCHA_LUA_PATH = "send_captcha.lua";

    private final StringRedisTemplate stringRedisTemplate;

    private final List<CaptchaSenderStrategy> captchaSenderStrategies;

    private final VerifyCaptchaUtil verifyCaptchaUtil;

    private final Executor captchaExecutor;

    private final RedisScriptExecutor redisScriptExecutor;



    @Override
    public CaptchaVO verifyCaptcha(VerifyCodeFormRequest request) {
        boolean verified = verifyCaptchaUtil.verifyCaptcha(request.getCaptchaUsageType(), request.getIdentification(), request.getVerifyCode());
        if (!verified) {
            throw new BusinessException.ParamException(ExceptionMessage.CAPTCHA_ERROR);
        }
        // 返货验证标识VO
        if (hasCheckIssuer(request.getCaptchaUsageType())) {
            String issuer = UUID.randomUUID().toString().replace("-", "");
            String key =
                    RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX +
                            request.getCaptchaUsageType().getDisplayName() + ":" +
                            request.getIdentification() + ":" +
                            RedisConstants.AUTH_ISSUER;
            stringRedisTemplate.opsForValue().set(key, issuer, 15, TimeUnit.MINUTES);
            return new CaptchaVO(issuer);
        }
        return null;
    }

    private boolean hasCheckIssuer(CaptchaUsageType usageType) {
        return !usageType.equals(CaptchaUsageType.LOGIN);
    }

    @Override
    public void sendCaptcha(CaptchaRequest request) {
        // 生成验证码
        String captcha = RandomUtil.randomNumbers(6);

        // 构建 key
        String defendKey = RedisConstants.AUTH_USER_CAPTCHA_LOCK
                + request.getCaptchaUsageType().getDisplayName() + ":" +
                request.getIdentification();
        String captchaKey = RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX +
                request.getCaptchaUsageType().getDisplayName() + ":" +
                request.getIdentification() + ":" +
                RedisConstants.AUTH_IDENTIFICATION;

        // 执行 Lua 脚本原子操作
        // 参数顺序：KEYS=[defendKey, captchaKey], ARGV=[captcha, defendExpire, captchaExpire]
        Long result = redisScriptExecutor.executeForLong(
                SEND_CAPTCHA_LUA_PATH,
                List.of(defendKey, captchaKey),
                captcha,                    // ARGV[1]: 验证码值
                String.valueOf(60),         // ARGV[2]: 防刷过期时间 60秒
                String.valueOf(300)         // ARGV[3]: 验证码过期时间 300秒
        );

        // 根据返回值处理结果

        if (result == 2) {
            // 一分钟内重复请求验证码
            throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.CAPTCHA_ALREADY_EXISTS);
        }

        if (ObjectUtil.isNull(result) || result == 3) {
            // 其它错误
            throw new SystemException(ExceptionMessage.SYSTEM_ERROR);
        }

        // result == 1 表示成功，发送验证码
        captchaExecutor.execute(() -> sendCaptcha(request.getIdentification(), captcha, request.getCaptchaUsageType()));
    }

    private void sendCaptcha(String identification, String captcha, CaptchaUsageType usage) {
        captchaSenderStrategies.stream()
                .filter(captchaSenderStrategy -> captchaSenderStrategy.support(identification))
                .findFirst()
                .ifPresentOrElse(
                        captchaSenderStrategy -> captchaSenderStrategy.send(identification, captcha, usage),
                        () -> {
                            throw new BusinessException.ParamException(ExceptionMessage.PHONE_EMAIL_FORMAT_ERROR);
                        }
                );
    }


}
