package com.universe.life.common.server.api.service.impl;


import cn.hutool.core.util.RandomUtil;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.common.server.api.service.ICaptchaService;
import com.universe.life.common.server.api.strategy.CaptchaSenderStrategy;
import com.universe.life.common.server.model.domain.domain.dto.request.CaptchaRequest;
import com.universe.life.common.server.model.domain.domain.dto.request.VerifyCodeFormRequest;
import com.universe.life.common.server.model.domain.domain.enums.CaptchaUsageType;
import com.universe.life.common.server.model.domain.domain.vo.CaptchaVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements ICaptchaService {

    private final StringRedisTemplate stringRedisTemplate;

    private final List<CaptchaSenderStrategy> captchaSenderStrategies;

    private final VerifyCaptchaUtil verifyCaptchaUtil;

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
        // 先查看redis中是否有验证码
        String defendKey = RedisConstants.AUTH_USER_CAPTCHA_LOCK
                + request.getCaptchaUsageType().getDisplayName() + ":" +
                request.getIdentification();
        Boolean hasKey = stringRedisTemplate.hasKey(defendKey);
        if (hasKey) {
            throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.CAPTCHA_ALREADY_EXISTS);
        }
        // 生成验证码
        String captcha = RandomUtil.randomNumbers(6);
        // 发送验证码
        captchaSenderStrategies.stream()
                .filter(captchaSenderStrategy -> captchaSenderStrategy.support(request.getIdentification()))
                .findFirst()
                .ifPresentOrElse(
                        captchaSenderStrategy -> captchaSenderStrategy.send(request.getIdentification(), captcha, request.getCaptchaUsageType()),
                        () -> {
                            throw new BusinessException.ParamException(ExceptionMessage.PHONE_EMAIL_FORMAT_ERROR);
                        }
                );
        // 缓存验证码
        String key =
                RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX +
                        request.getCaptchaUsageType().getDisplayName() + ":" +
                        request.getIdentification() + ":" +
                        RedisConstants.AUTH_IDENTIFICATION;

        stringRedisTemplate.opsForValue().set(key, captcha, 5, TimeUnit.MINUTES);
        // 缓存验证码防刷时间
        stringRedisTemplate.opsForValue().setIfAbsent(
                defendKey,
                "1", 1, TimeUnit.MINUTES);
    }


}
