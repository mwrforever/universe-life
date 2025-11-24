package com.universe.life.auth.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.universe.life.api.client.UserClient;
import com.universe.life.auth.service.constants.RedisConstants;
import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;
import com.universe.life.auth.service.domain.vo.UserLoginVO;
import com.universe.life.auth.service.service.IAuthUserService;
import com.universe.life.common.exception.AuthException;
import com.universe.life.common.message.ExceptionMessage;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 认证用户服务实现类
 *
 * <p>提供基于Spring Security Authorization Server的完整认证服务，
 * 支持手动构建OAuth2 token，简化内部项目登录流程。</p>
 *
 * @author 毛伟然
 * @since 2025/11/13 14:51
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements IAuthUserService {

    private final UserClient userClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder bcryptPasswordEncoder;


    @Override
    public void register(RegisterFormRequest request) {
        // 用户注册
        // 先在redis中查看当前验证的业务标识
        String key = RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX +
                request.getCaptchaUsageType().getDisplayName() + ":" +
                request.getIdentification();
        Object codeObj = stringRedisTemplate.opsForHash().get(
                key,
                RedisConstants.AUTH_ISSUER
        );
        // 如果验证码已过期，则返回错误
        if (ObjectUtil.isNull(codeObj)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTHORIZATION_CODE_EXPIRED);
        }
        // 校验授权码与当前业务标识的验证码是否一致
        if (!String.valueOf(codeObj).equals(request.getIssuer())) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTHORIZATION_CODE_INVALID);
        }
        // 删除issuer标识
        stringRedisTemplate.opsForHash().delete(
                key,
                RedisConstants.AUTH_ISSUER
        );
        // 封装用户数据
        RegisterFormDTO registerFormDTO = BeanUtil.toBean(request, RegisterFormDTO.class);
        // 对密码进行加密处理
        registerFormDTO.setPassword(bcryptPasswordEncoder.encode(registerFormDTO.getPassword()));
        // 保存用户数据
        userClient.add(registerFormDTO);
    }


    /**
     * 构建用户登录响应对象
     *
     * <p>将OAuth2令牌信息转换为前端需要的UserLoginVO格式。</p>
     *
     * @param accessToken  访问令牌
     * @param refreshToken 刷新令牌
     * @return 用户登录响应对象
     */
    private UserLoginVO buildUserLoginVO(OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
        Instant now = Instant.now();
        long expiresIn = accessToken.getExpiresAt() != null ?
                accessToken.getExpiresAt().getEpochSecond() - now.getEpochSecond() : 3600;

        return new UserLoginVO(
                accessToken.getTokenValue(),
                refreshToken != null ? refreshToken.getTokenValue() : null,
                String.valueOf(expiresIn),
                accessToken.getTokenType().getValue()
        );
    }

}
