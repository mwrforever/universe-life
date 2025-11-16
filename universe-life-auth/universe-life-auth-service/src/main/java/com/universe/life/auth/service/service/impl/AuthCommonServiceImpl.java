package com.universe.life.auth.service.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.universe.life.auth.resource.domain.dto.request.CaptchaRequest;
import com.universe.life.auth.resource.domain.vo.CaptchaVO;
import com.universe.life.auth.service.constants.RedisConstants;
import com.universe.life.auth.service.service.IAuthCommonService;
import com.universe.life.common.exception.BusinessException;
import com.universe.life.common.message.ExceptionMessage;
import com.universe.life.common.strategy.CaptchaSenderStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:10
 */
@Service
@RequiredArgsConstructor
public class AuthCommonServiceImpl implements IAuthCommonService {

    private final StringRedisTemplate stringRedisTemplate;

    private final List<CaptchaSenderStrategy> captchaSenderStrategies;

    @Override
    public CaptchaVO sendCaptcha(CaptchaRequest request) {
        // 先查看redis中是否有验证码
        Boolean hasKey = stringRedisTemplate.hasKey(RedisConstants.AUTH_USER_CAPTCHA_LOCK + request.getIdentification());
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
                        captchaSenderStrategy -> captchaSenderStrategy.send(request.getIdentification(), captcha),
                        () -> {
                            throw new BusinessException.ParamException(ExceptionMessage.PHONE_EMAIL_FORMAT_ERROR);
                        }
                );
        // 缓存到redis并设置5分钟有效期
        stringRedisTemplate.opsForHash().put(
                RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX + request.getIdentification(),
                RedisConstants.AUTH_IDENTIFICATION,
                captcha
        );
        // 生成验证唯一标识
        String issuer = UUID.randomUUID().toString().replace("-", "");
        // 缓存这个验证唯一标识
        stringRedisTemplate.opsForHash().put(
                RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX + request.getIdentification(),
                RedisConstants.AUTH_IDENTIFICATION,
                issuer
        );
        // 设置过期时间
        stringRedisTemplate.expire(RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX + request.getIdentification(), 5, TimeUnit.MINUTES);
        // 缓存验证码标识
        stringRedisTemplate.opsForValue().set(RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX + request.getIdentification(), captcha, 5, TimeUnit.MINUTES);
        // 缓存验证码防刷时间
        stringRedisTemplate.opsForValue().setIfAbsent(RedisConstants.AUTH_USER_CAPTCHA_LOCK + request.getIdentification(), "1", 1, TimeUnit.MINUTES);
        // 封装VO返回结果
        return new CaptchaVO(captcha, issuer);
    }
}
