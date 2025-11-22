package com.universe.life.auth.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.universe.life.api.client.UserClient;
import com.universe.life.auth.resource.domain.dto.request.VerifyFormRequest;
import com.universe.life.auth.service.constants.RedisConstants;
import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;
import com.universe.life.auth.service.domain.vo.UserLoginVO;
import com.universe.life.auth.service.properties.AuthorizationServerProperties;
import com.universe.life.auth.service.service.IAuthUserService;
import com.universe.life.common.domain.Result;
import com.universe.life.common.domain.dto.UserAuthInfo;
import com.universe.life.common.exception.AuthException;
import com.universe.life.common.message.ExceptionMessage;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

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

    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final RegisteredClientRepository clientRepository;
    private final OAuth2AuthorizationService authorizationService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthorizationServerProperties authorizationServerProperties;
    private final UserClient userClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder bcryptPasswordEncoder;

    @Override
    public Result<UserLoginVO> login(VerifyFormRequest verifyFormRequest) {
        log.info("开始处理用户登录请求，用户标识: {}", verifyFormRequest.getIdentification());

        try {
            // 1. 创建认证令牌并进行用户认证
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            verifyFormRequest.getIdentification(),
                            verifyFormRequest.getVerifyCode()
                    );

            AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            log.info("用户认证成功: {}", authentication.getName());

            // 2. 获取已注册的客户端信息
            RegisteredClient client = clientRepository.findByClientId(authorizationServerProperties.getClientId());
            if (client == null) {
                log.error("未找到客户端: {}", authorizationServerProperties.getClientId());
                throw new AuthException.AuthenticationException("客户端不存在");
            }

            // 3. 获取已认证的用户信息
            UserAuthInfo userAuthInfo = (UserAuthInfo) authentication.getPrincipal();
            if (userAuthInfo == null) {
                log.error("无法获取用户认证信息");
                throw new AuthException.AuthenticationException("用户认证信息不存在");
            }

            // 4. 构建OAuth2授权对象，包含用户信息
            Set<String> authorizedScopes = client.getScopes();

            OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(client)
                    .id(UUID.randomUUID().toString().replace("-", ""))
                    .principalName(authentication.getName())
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD) // 使用密码模式
                    .authorizedScopes(authorizedScopes)
                    // 添加用户信息到OAuth2Authorization，这些信息会被包含在JWT中
                    .attribute("user_id", userAuthInfo.getId())
                    .build();

            // 5. 生成访问令牌
            OAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(client)
                    .principal(authentication)
                    .authorization(authorization)
                    .authorizedScopes(authorizedScopes)
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                    .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                    .build();

            OAuth2Token generatedAccessToken = tokenGenerator.generate(accessTokenContext);

            OAuth2AccessToken accessToken;
            if (generatedAccessToken instanceof Jwt jwt) {
                // 特殊情况：返回的是原始Jwt对象，需要手动包装
                log.info("生成的是原始Jwt对象，手动包装为OAuth2AccessToken");

                // 手动创建OAuth2AccessToken
                Instant issuedAt = jwt.getIssuedAt();
                accessToken = new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        jwt.getTokenValue(),
                        issuedAt,
                        Instant.now().plusSeconds(3600),
                        authorizedScopes
                );
            } else {
                log.error("访问令牌生成失败，返回类型: {}",
                        generatedAccessToken != null ? generatedAccessToken.getClass().getName() : "null");
                throw new AuthException.AuthenticationException("令牌生成失败");
            }
            log.info("成功生成访问令牌，包含用户信息: user_id={}, username={}",
                    userAuthInfo.getId(), userAuthInfo.getUsername());

            // 6. 更新授权对象，添加访问令牌
            authorization = OAuth2Authorization.from(authorization)
                    .accessToken(accessToken)
                    .build();

            // 7. 生成刷新令牌
            OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(client)
                    .principal(authentication)
                    .authorization(authorization)
                    .authorizedScopes(authorizedScopes)
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .build();

            OAuth2Token generatedRefreshToken = tokenGenerator.generate(refreshTokenContext);
            if (!(generatedRefreshToken instanceof OAuth2RefreshToken refreshToken)) {
                log.error("刷新令牌生成失败");
                throw new AuthException.AuthenticationException("令牌生成失败");
            }

            // 8. 更新授权对象，添加刷新令牌
            authorization = OAuth2Authorization.from(authorization)
                    .refreshToken(refreshToken)
                    .build();

            // 9. 持久化授权信息
            authorizationService.save(authorization);

            // 10. 构建用户登录响应对象
            UserLoginVO userLoginVO = buildUserLoginVO(accessToken, refreshToken);
            return Result.success(userLoginVO);

        } catch (Exception e) {
            log.error("用户登录失败，用户标识: {}", verifyFormRequest.getIdentification(), e);
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTH_FAILED);
        }
    }

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
