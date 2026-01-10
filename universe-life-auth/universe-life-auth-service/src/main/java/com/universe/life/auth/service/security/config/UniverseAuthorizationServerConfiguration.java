package com.universe.life.auth.service.security.config;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.universe.life.auth.common.domain.dto.UserAuthInfo;
import com.universe.life.auth.resource.util.CommonSecurityConfigUtil;
import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.auth.service.properties.AuthorizationServerProperties;
import com.universe.life.auth.service.properties.JwkProperties;
import com.universe.life.auth.service.security.CollectionMixins;
import com.universe.life.auth.service.security.CustomSecurityMixin;
import com.universe.life.auth.service.security.TokenCustomizer;
import com.universe.life.auth.service.security.convert.PublicClientRefreshTokenAuthenticationConverter;
import com.universe.life.auth.service.security.convert.PublicClientRevocationAuthenticationConverter;
import com.universe.life.auth.service.security.provider.PublicClientRefreshTokenAuthenticationProvider;
import com.universe.life.auth.service.security.token.CustomRefreshTokenGenerator;
import com.universe.life.auth.service.security.token.SmsAuthenticationToken;
import com.universe.life.auth.service.security.token.UsernamePasswordAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.ClientSecretAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.PublicClientAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.oauth2.server.authorization.web.authentication.ClientSecretBasicAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.ClientSecretPostAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.PublicClientAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * @author 毛伟然
 * @since 2025/11/8 10:48
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({AuthorizationServerProperties.class, JwkProperties.class})
@RequiredArgsConstructor
public class UniverseAuthorizationServerConfiguration {

    private final AuthorizationServerProperties authorizationServerProperties;

    private final AccessDeniedHandler jwtAccessDeniedHandler;

    private final JwkProperties jwkProperties;

    @Bean
    public JwkManager jwkManager() {
        log.info("创建JwkManager对象");
        return new JwkManager(jwkProperties);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }


    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return new TokenCustomizer();
    }


    /**
     * 授权服务器基本设置Bean
     * <p>配置OAuth2授权服务器的核心参数，主要是issuer URI。
     * issuer是授权服务器的唯一标识符，用于JWT令牌验证和发现服务。</p>
     * 可通过spring.security.oauth2.authorizationserver.issuer配置
     *
     * @return 配置好的AuthorizationServerSettings对象
     */
    @Bean  // Spring注解：将方法返回的对象注册为Spring容器中的Bean
    public AuthorizationServerSettings authorizationServerSettings() {

        // 记录配置信息到日志，便于调试和监控
        log.info("配置授权服务器设置 - Issuer: {}", authorizationServerProperties.getIssuer());

        // 使用建造者模式创建AuthorizationServerSettings对象
        return AuthorizationServerSettings.builder()
                .issuer(authorizationServerProperties.getIssuer())  // 设置授权服务器的issuer URI
                .build();  // 构建最终的配置对象
    }


    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(
            JWKSource<SecurityContext> jwkSource,
            OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer) {

        // 1. 构建 Access Token 生成器 (JWT)
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        // 注入你之前写的 Customizer，保证 Access Token 里的 user_id 正常
        jwtGenerator.setJwtCustomizer(tokenCustomizer);

        // 2. 构建 Access Token 生成器
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();

        // 3. 构建我们自定义的 Refresh Token 生成器
        CustomRefreshTokenGenerator refreshTokenGenerator = new CustomRefreshTokenGenerator();

        // 4. 组合起来
        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator,
                accessTokenGenerator,
                refreshTokenGenerator
        );
    }

    @Bean
    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository
    ) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationService oAuth2AuthorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {

        // 1. 创建 Service 实例
        JdbcOAuth2AuthorizationService service =
                new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);

        // 2. 创建并配置 RowMapper
        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);

        // 3. 配置 Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();

        // 3.0 配置安全的类型处理 - 解决Long类型白名单问题的关键步骤
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("java.lang.Long")  // 明确允许Long类型
                .allowIfSubType("java.lang.Integer")
                .allowIfSubType("java.lang.String")
                .allowIfSubType("com.universe.life.")  // 允许项目包下的所有类
                .allowIfSubType("org.springframework.security.")  // 允许Spring Security类
                .allowIfSubType("java.time.")  // 允许时间相关类
                .allowIfSubType("java.util.")  // 允许工具类
                .build();

        // 启用默认类型处理，使用PROPERTY格式以兼容现有数据
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 3.1 注册 Spring Security 默认模块 (核心安全类)
        List<Module> modules = SecurityJackson2Modules.getModules(classLoader);
        objectMapper.registerModules(modules);

        // 3.2 注册 OAuth2 Authorization Server 模块
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());

        // 3.3 注册Java时间模块
        objectMapper.registerModule(new JavaTimeModule());

        // 3.4 创建自定义模块处理Long类型
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, new JsonSerializer<>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                if (value != null) {
                    gen.writeString(value.toString());
                }
            }
        });

        longModule.addDeserializer(Long.class, new JsonDeserializer<>() {
            @Override
            public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getValueAsString();
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                return Long.valueOf(text);
            }
        });

        objectMapper.registerModule(longModule);

        // 3.5 注册你的自定义类
        // 注册自定义的 Token 类
        objectMapper.addMixIn(UsernamePasswordAuthenticationToken.class, CustomSecurityMixin.class);
        objectMapper.addMixIn(SmsAuthenticationToken.class, CustomSecurityMixin.class);

        // 注册自定义的 User/Principal 类 (日志里显示是 UserAuthInfo，这个也必须加，否则修好了Token就会报这个错)
        objectMapper.addMixIn(UserAuthInfo.class, CustomSecurityMixin.class);


        // 3.6 配置额外的反序列化选项以处理类型格式兼容性问题
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 3.7 为Map类型配置特殊的Mixin以解决类型信息格式问题
        // 使用接口和公共基类替代不可访问的内部类
        objectMapper.addMixIn(Map.class, CollectionMixins.MapMixin.class);
        objectMapper.addMixIn(List.class, CollectionMixins.ListMixin.class);
        objectMapper.addMixIn(Collection.class, CollectionMixins.ListMixin.class);
        // 4. 将配置好的 ObjectMapper 设置给 RowMapper
        rowMapper.setObjectMapper(objectMapper);
        // 5. 将 RowMapper 设置给 Service
        service.setAuthorizationRowMapper(rowMapper);

        return service;
    }


    /**
     * 客户端注册仓库Bean - 生产环境数据库存储
     *
     * <p>配置OAuth2客户端信息的持久化存储，使用JdbcOperations进行数据库操作。
     * 支持动态客户端注册和管理，自动初始化默认客户端。</p>
     *
     * @param jdbcTemplate Spring的JDBC操作模板，用于执行SQL语句
     * @return 配置好的RegisteredClientRepository对象
     */
    @Bean  // Spring注解：注册客户端仓库Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {

        // 记录初始化信息到日志
        log.info("初始化客户端注册仓库 - 数据库模式");

        // 创建基于数据库的客户端注册仓库
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

        // 检查默认客户端是否已存在，避免重复创建
        for (AuthorizationServerProperties.AuthorizationServerPropertiesConfig config : authorizationServerProperties.getConfigs()) {
            if (!isClientExists(repository, config.getClientId())) {
                // 判断是否为员工客户端，使用不同的scope配置
                // 创建客户端配置
                RegisteredClient client = createGatewayClient(
                        config.getClientId(),
                        config.getClientSecret(),
                        config.getRedirectUri(),
                        config.getPostLogoutRedirectUri()
                );
                // 保存客户端到数据库
                repository.save(client);
            }
        }
        // 返回配置好的客户端仓库
        return repository;
    }

    /**
     * 创建网关客户端配置
     *
     * <p>构建一个完整的OAuth2客户端配置，包括认证方式、授权类型、权限范围等。
     * 根据是否有 clientSecret 自动判断创建公共客户端还是机密客户端。
     * 员工客户端不包含 openid scope，避免刷新 token 时需要 id_token。</p>
     *
     * @param clientId              客户端标识符
     * @param clientSecret          客户端密钥（可选，为 null 则创建公共客户端）
     * @param redirectUris          授权回调URI
     * @param postLogoutRedirectUri 登出后重定向URI
     * @return 配置完成的RegisteredClient对象
     */
    private RegisteredClient createGatewayClient(
            String clientId,
            String clientSecret,
            Collection<String> redirectUris,
            String postLogoutRedirectUri
    ) {
        // 判断是否为公共客户端（无 clientSecret）
        boolean isPublicClient = clientSecret == null || clientSecret.trim().isEmpty();


        RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString().replace("-", ""))
                .clientId(clientId);

        // 根据是否有密钥设置认证方式
        if (isPublicClient) {
            // 公共客户端：不需要密钥认证
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
        } else {
            // 机密客户端：使用 POST 方式认证
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .clientSecret(clientSecret);
        }

        builder.authorizationGrantTypes(at -> at.addAll(List.of(
                        AuthorizationGrantType.AUTHORIZATION_CODE,
                        AuthorizationGrantType.REFRESH_TOKEN
                )))
                .redirectUris(uris -> uris.addAll(redirectUris))
                .postLogoutRedirectUri(postLogoutRedirectUri)
                .scopes(scopes -> {
                    // 用户客户端：包含完整的 OIDC scope
                    scopes.addAll(Arrays.asList(
                            OidcScopes.OPENID,
                            OidcScopes.PROFILE,
                            OidcScopes.EMAIL,
                            OidcScopes.PHONE,
                            "offline_access",
                            "user_info"
                    ));
                })
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).requireProofKey(true).build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(2))     // 访问令牌 2 小时
                        .refreshTokenTimeToLive(Duration.ofDays(7))     // 刷新令牌 7 天
                        .reuseRefreshTokens(false)                       // 刷新后生成新的 refresh_token
                        .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                        .build());
        return builder.build();
    }

    /**
     * 检查指定客户端是否已在数据库中存在
     *
     * <p>通过客户端ID查询数据库，避免重复创建相同的客户端。
     * 使用异常处理机制来判断客户端是否存在。</p>
     *
     * @param repository 客户端注册仓库对象
     * @param clientId   要检查的客户端ID
     * @return 如果客户端存在返回true，否则返回false
     */
    private boolean isClientExists(JdbcRegisteredClientRepository repository, String clientId) {
        try {
            // 尝试通过客户端ID查找客户端
            RegisteredClient client = repository.findByClientId(clientId);
            // 如果没有抛出异常，说明客户端存在
            return ObjectUtil.isNotNull(client);
        } catch (Exception e) {
            // 如果抛出异常（如EmptyResultDataAccessException），说明客户端不存在
            return false;
        }
    }


    /**
     * 授权服务器安全过滤器链 - 优先级为2
     * 处理所有OAuth2授权服务器端点请求
     */
    @Bean
    @Order(2)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            AuthorizationServerSettings authorizationServerSettings,
            RegisteredClientRepository registeredClientRepository,
            OAuth2AuthorizationService oAuth2AuthorizationService,
            HttpSessionRequestCache httpSessionRequestCache
    ) throws Exception {

        log.info("配置授权服务器安全过滤器链");
        // 创建授权服务器配置器，用于自定义OAuth2端点行为
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer.oidc(Customizer.withDefaults());
        return http
                // 设置安全匹配器，只处理授权服务器端点请求
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(requests -> requests
                        .anyRequest().authenticated())
                //                 应用授权服务器配置
                .with(authorizationServerConfigurer, configurer ->
                        configurer.authorizationServerSettings(authorizationServerSettings)
                                .clientAuthentication(clientAuth -> {
                                            // 1. 设置转换器
                                            DelegatingAuthenticationConverter delegatingConverter = getDelegatingAuthenticationConverter();
                                            clientAuth.authenticationConverter(delegatingConverter);

                                            // 2. 添加 Provider
                                            // 必须把处理公共客户端的标准 Provider 加回来！
                                            clientAuth.authenticationProvider(new PublicClientAuthenticationProvider(registeredClientRepository, oAuth2AuthorizationService));

                                            // 为了保证带 Secret 的客户端也能正常工作，把这个也加回来
                                            clientAuth.authenticationProvider(new ClientSecretAuthenticationProvider(registeredClientRepository, oAuth2AuthorizationService));

                                            // 3. 最后加上你自定义的 Provider
                                            clientAuth.authenticationProvider(new PublicClientRefreshTokenAuthenticationProvider(registeredClientRepository));
                                        }
                                )
                                .authorizationEndpoint(authorizationEndpoint ->
                                        authorizationEndpoint.consentPage(authorizationServerProperties.getConsentPage()))
                )
                .requestCache(c -> c.requestCache(httpSessionRequestCache))
                // 配置CSRF保护 - 对授权服务器端点禁用CSRF（符合OAuth2标准）
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                // 配置异常处理 - 设置认证入口点和访问拒绝处理器
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                // 配置HTTP安全头 - 开发阶段简化配置，避免影响前后端分离开发
                .headers(CommonSecurityConfigUtil::getPermissionsPolicyConfig)
                .build();  // 构建并返回安全过滤器链对象
    }

    @NotNull
    private static DelegatingAuthenticationConverter getDelegatingAuthenticationConverter() {
        List<AuthenticationConverter> converters = Arrays.asList(
                new ClientSecretBasicAuthenticationConverter(),
                new ClientSecretPostAuthenticationConverter(),
                new PublicClientAuthenticationConverter(), // 这个用于处理 /revoke 的公共客户端请求
                new PublicClientRefreshTokenAuthenticationConverter(), // 你自定义的，用于处理 Refresh Token
                new PublicClientRevocationAuthenticationConverter() // 你自定义的，用于处理 Revoke Token
        );
        // 2. 组合成一个代理转换器
        return new DelegatingAuthenticationConverter(converters);
    }


    /**
     * HTTP会话事件发布器Bean
     *
     * <p>发布HTTP会话生命周期事件，支持会话管理和监控。
     * 主要用于会话跟踪和会话安全防护。</p>
     *
     * @return HttpSessionEventPublisher实例
     */
    @Bean  // Spring注解：注册会话事件发布器Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        // 创建HTTP会话事件发布器，用于监听会话生命周期事件
        return new HttpSessionEventPublisher();
    }

    /**
     * 授权服务器健康检查指示器Bean
     *
     * <p>提供授权服务器的健康状态检查，集成到Spring Boot Actuator。
     * 支持Kubernetes健康检查、负载均衡器健康探测等运维功能。</p>
     *
     * @return HealthIndicator实现
     */
    @Bean  // Spring注解：注册健康检查指示器Bean
    public HealthIndicator authorizationServerHealthIndicator() {
        // 创建健康检查响应，使用lambda表达式简化代码
        return () -> Health.up()
                .withDetail("status", "授权服务器运行正常")  // 添加状态详情
                .withDetail("timestamp", System.currentTimeMillis())  // 添加检查时间戳
                .build();  // 构建健康状态对象
    }

}
