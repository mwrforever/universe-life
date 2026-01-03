package com.universe.life.auth.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.universe.life.api.client.UserClient;
import com.universe.life.auth.common.constants.JwtConstants;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.auth.service.domain.dto.request.EmployeeCaptchaLoginRequest;
import com.universe.life.auth.service.domain.dto.request.EmployeeLoginRequest;
import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;
import com.universe.life.auth.service.domain.vo.UserLoginVO;
import com.universe.life.auth.service.security.token.SmsAuthenticationToken;
import com.universe.life.auth.service.security.token.UsernamePasswordAuthenticationToken;
import com.universe.life.auth.service.service.IAuthUserService;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    private final UserClient userClient;
    private final PasswordEncoder bcryptPasswordEncoder;
    private final VerifyCaptchaUtil verifyCaptchaUtil;
    private final AuthenticationManager authenticationManager;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final RegisteredClientRepository registeredClientRepository;
    private final AuthorizationServerSettings authorizationServerSettings;

    /**
     * 员工客户端ID
     */
    private static final String EMPLOYEE_CLIENT_ID = "Kp7vR9mNxq2L8tQwYzba";


    @Override
    public void register(RegisterFormRequest request) {
        // 用户注册
        boolean success = verifyCaptchaUtil.verifyCaptchaIssuer(request.getCaptchaUsageType(), request.getIdentification(), request.getIssuer());
        if (!success) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTHORIZATION_CODE_EXPIRED);
        }
        // 封装用户数据
        RegisterFormDTO registerFormDTO = BeanUtil.toBean(request, RegisterFormDTO.class);
        // 对密码进行加密处理
        registerFormDTO.setPassword(bcryptPasswordEncoder.encode(registerFormDTO.getPassword()));
        // 保存用户数据
        userClient.add(registerFormDTO);
    }

    @Override
    public UserLoginVO employeeLogin(EmployeeLoginRequest request) {
        log.info("员工登录请求 - 用户名: {}", request.getIdentification());

        try {
            // 1. 使用 AuthenticationManager 进行认证
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getIdentification(), request.getPassword(), JwtConstants.EMPLOYEE_LOGIN);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 2. 生成 Token 并保存授权记录
            return generateTokensAndSaveAuthorization(authentication, request.getIdentification());

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            log.error("员工登录失败 - 用户名: {}, 错误: {}", request.getIdentification(), e.getMessage(), e);
            throw new AuthException.AuthenticationException("用户名或密码错误");
        } catch (Exception e) {
            log.error("员工登录失败 - 用户名: {}, 错误: {}", request.getIdentification(), e.getMessage(), e);
            throw new AuthException.AuthenticationException("登录失败");
        }
    }

    @Override
    public UserLoginVO employeeCaptchaLogin(EmployeeCaptchaLoginRequest request) {
        log.info("员工验证码登录请求 - 用户标识: {}", request.getIdentification());

        SmsAuthenticationToken authenticationToken = new SmsAuthenticationToken(request.getIdentification(), request.getCaptcha(), request.getCaptchaUsageType(), JwtConstants.EMPLOYEE_LOGIN);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 4. 生成 Token 并保存授权记录
        return generateTokensAndSaveAuthorization(authentication, request.getIdentification());
    }

    /**
     * 生成 Token 并保存授权记录（公共方法）
     *
     * @param authentication 用户认证信息
     * @param principalName  主体名称（用于日志）
     * @return 登录响应信息
     */
    private UserLoginVO generateTokensAndSaveAuthorization(Authentication authentication, String principalName) {
        // 1. 获取员工客户端配置
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(EMPLOYEE_CLIENT_ID);
        if (registeredClient == null) {
            log.error("员工客户端不存在: {}", EMPLOYEE_CLIENT_ID);
            throw new AuthException.AuthenticationException("系统配置错误");
        }

        // 2. 生成唯一授权ID
        String authorizationId = UUID.randomUUID().toString();

        // 3. 创建授权服务器上下文
        AuthorizationServerContext ctx = new AuthorizationServerContext() {
            @Override
            public String getIssuer() {
                return authorizationServerSettings.getIssuer();
            }

            @Override
            public AuthorizationServerSettings getAuthorizationServerSettings() {
                return authorizationServerSettings;
            }
        };

        // 4. 生成 Access Token
        OAuth2AccessToken accessToken = generateAccessToken(
                registeredClient,
                authentication,
                ctx
        );

        if (accessToken == null) {
            throw new AuthException.AuthenticationException("令牌生成失败");
        }

        // 5. 生成 Refresh Token
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            refreshToken = generateRefreshToken(
                    registeredClient,
                    authentication,
                    ctx
            );
        }

        // 6. 构建 OAuth2Authorization 对象
        OAuth2Authorization authorization = buildAuthorization(
                authorizationId,
                registeredClient,
                authentication,
                accessToken,
                refreshToken
        );

        // 7. 保存授权记录
        authorizationService.save(authorization);

        log.info("Token生成成功 - 主体: {}, Token类型: {}, 过期时间: {}",
                principalName,
                accessToken.getTokenType().getValue(),
                accessToken.getExpiresAt());

        // 8. 构建返回结果
        return buildLoginResponse(accessToken, refreshToken);
    }

    /**
     * 生成 Access Token
     */
    private OAuth2AccessToken generateAccessToken(
            RegisteredClient registeredClient,
            Authentication authentication,
            AuthorizationServerContext authorizationServerContext) {

        DefaultOAuth2TokenContext.Builder contextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(authentication)
                .authorizationServerContext(authorizationServerContext)
                .authorizedScopes(registeredClient.getScopes())
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);

        // tokenGenerator 可能返回 Jwt 或 OAuth2AccessToken
        Object token = tokenGenerator.generate(contextBuilder.build());

        // 如果是 JWT token，需要转换为 OAuth2AccessToken
        if (token instanceof Jwt jwt) {
            return new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    jwt.getTokenValue(),
                    jwt.getIssuedAt(),
                    jwt.getExpiresAt(),
                    registeredClient.getScopes()
            );
        }

        // 否则直接转换为 OAuth2AccessToken
        return (OAuth2AccessToken) token;
    }

    /**
     * 生成 Refresh Token
     */
    private OAuth2RefreshToken generateRefreshToken(
            RegisteredClient registeredClient,
            Authentication authentication,
            AuthorizationServerContext authorizationServerContext) {

        DefaultOAuth2TokenContext.Builder contextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(authentication)
                .authorizationServerContext(authorizationServerContext)
                .authorizedScopes(registeredClient.getScopes())
                .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);

        // tokenGenerator 可能返回不同类型的 token
        Object token = tokenGenerator.generate(contextBuilder.build());

        // 如果是 JWT token，需要转换为 OAuth2RefreshToken
        if (token instanceof Jwt jwt) {
            return new OAuth2RefreshToken(
                    jwt.getTokenValue(),
                    jwt.getIssuedAt(),
                    jwt.getExpiresAt()
            );
        }

        // 否则直接转换为 OAuth2RefreshToken
        return (OAuth2RefreshToken) token;
    }

    /**
     * 构建 OAuth2Authorization 对象
     */
    private OAuth2Authorization buildAuthorization(
            String authorizationId,
            RegisteredClient registeredClient,
            Authentication authentication,
            OAuth2AccessToken accessToken,
            OAuth2RefreshToken refreshToken) {

        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(authorizationId)
                .principalName(authentication.getName())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizedScopes(registeredClient.getScopes())
                .attribute(Principal.class.getName(), authentication)
                .attribute("authorized_scopes", registeredClient.getScopes())
                .accessToken(accessToken);

        if (refreshToken != null) {
            builder.refreshToken(refreshToken);
        }

        return builder.build();
    }

    /**
     * 构建登录响应对象
     * <p>
     * 符合 OAuth 2.0 RFC 6749 规范,expires_in 返回从响应发出时算起的过期秒数
     */
    private UserLoginVO buildLoginResponse(OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
        UserLoginVO result = new UserLoginVO();
        result.setAccessToken(accessToken.getTokenValue());
        result.setRefreshToken(refreshToken != null ? refreshToken.getTokenValue() : null);
        if (accessToken.getExpiresAt() != null) {
            // OAuth 2.0 标准要求 expiresIn 返回剩余秒数,而非绝对时间戳
            long expiresIn = ChronoUnit.SECONDS.between(
                Instant.now(),
                accessToken.getExpiresAt()
            );
            result.setExpiresIn(String.valueOf(expiresIn));
        }
        result.setTokenType(accessToken.getTokenType().getValue());
        return result;
    }

}
