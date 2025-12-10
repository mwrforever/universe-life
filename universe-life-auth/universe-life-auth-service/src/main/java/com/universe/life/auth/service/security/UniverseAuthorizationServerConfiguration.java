package com.universe.life.auth.service.security;

import cn.hutool.core.util.ObjectUtil;
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
import com.universe.life.auth.resource.util.CommonSecurityConfigUtil;
import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.auth.service.properties.AuthorizationServerProperties;
import com.universe.life.auth.service.properties.JwkProperties;
import com.universe.life.auth.service.security.convert.PublicClientRefreshTokenAuthenticationConverter;
import com.universe.life.auth.service.security.convert.PublicClientRevocationAuthenticationConverter;
import com.universe.life.auth.service.security.filter.MyUsernamePasswordAuthenticationFilter;
import com.universe.life.auth.service.security.filter.SmsAuthenticationFilter;
import com.universe.life.auth.service.security.provider.PublicClientRefreshTokenAuthenticationProvider;
import com.universe.life.auth.service.security.provider.SmsAuthenticationProvider;
import com.universe.life.auth.service.security.provider.UsernamePasswordAuthenticationProvider;
import com.universe.life.auth.service.security.token.CustomRefreshTokenGenerator;
import com.universe.life.auth.service.security.token.SmsAuthenticationToken;
import com.universe.life.auth.service.security.token.UsernamePasswordAuthenticationToken;
import com.universe.life.auth.service.service.impl.AuthCommonServiceImpl;
import com.universe.life.common.domain.dto.UserAuthInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

    private final PasswordEncoder bCryptPasswordEncoder;

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

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
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
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);

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
        objectMapper.addMixIn(java.util.Map.class, CollectionMixins.MapMixin.class);
        objectMapper.addMixIn(java.util.List.class, CollectionMixins.ListMixin.class);
        objectMapper.addMixIn(java.util.Collection.class, CollectionMixins.ListMixin.class);
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
        if (!isClientExists(repository, authorizationServerProperties.getClientId())) {
            // 创建网关客户端配置
            RegisteredClient gatewayClient = createGatewayClient(
                    authorizationServerProperties.getClientId(),
                    authorizationServerProperties.getRedirectUri(),
                    authorizationServerProperties.getPostLogoutRedirectUri()
            );
            // 保存客户端到数据库
            repository.save(gatewayClient);
            log.info("已创建网关客户端: {}", authorizationServerProperties.getClientId());
        }
        // 返回配置好的客户端仓库
        return repository;
    }

    /**
     * 创建网关客户端配置
     *
     * <p>构建一个完整的OAuth2客户端配置，包括认证方式、授权类型、权限范围等。
     * 此客户端主要用于网关服务的OAuth2集成。</p>
     *
     * @param clientId    客户端标识符
     * @param redirectUri 授权回调URI
     * @return 配置完成的RegisteredClient对象
     */
    private RegisteredClient createGatewayClient(
            String clientId,
            Collection<String> redirectUri,
            String postLogoutRedirectUri
    ) {
        // 使用建造者模式创建RegisteredClient对象
        return RegisteredClient.withId(UUID.randomUUID().toString().replace("-", ""))  // 生成唯一客户端ID
                .clientId(clientId)  // 设置客户端标识符
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)  // 客户端认证方式：HTTP Basic认证
                .authorizationGrantTypes(grants -> grants.addAll(Arrays.asList(  // 支持的授权类型
                        AuthorizationGrantType.AUTHORIZATION_CODE,  // 授权码模式，最安全的OAuth2流程
                        AuthorizationGrantType.REFRESH_TOKEN  // 刷新令牌模式，支持令牌续期
                )))
                .redirectUris(uris -> uris.addAll(redirectUri))
                .postLogoutRedirectUri(postLogoutRedirectUri)
                .scopes(scopes -> scopes.addAll(Arrays.asList(  // 支持的权限范围
                        OidcScopes.OPENID,  // OpenID Connect标准范围，获取用户标识
                        OidcScopes.PROFILE,  // 用户基本信息范围
                        OidcScopes.EMAIL,  // 用户邮箱范围
                        OidcScopes.PHONE,  // 用户电话范围
                        "offline_access",
                        "read",  // 读权限，自定义业务权限
                        "write",  // 写权限，自定义业务权限
                        "admin",  // 管理员权限
                        "trust",
                        "user_info"// 信任权限，用于特殊操作
                )))
                .clientSettings(ClientSettings.builder()  // 客户端设置
                        .requireAuthorizationConsent(false)  // 不要求用户授权同意，适用于可信客户端
                        .requireProofKey(true)  // 启用PKCE，增强授权码模式安全性
                        .build())
                .tokenSettings(TokenSettings.builder()  // 令牌设置
                        .accessTokenTimeToLive(Duration.ofHours(2))  // 访问令牌有效期：2 小时
                        .refreshTokenTimeToLive(Duration.ofDays(7))  // 刷新令牌有效期：30天
                        .reuseRefreshTokens(false)  // 不重复使用刷新令牌，增强安全性
                        .authorizationCodeTimeToLive(Duration.ofMinutes(5))  // 授权码有效期：5分钟
                        .build())
                .build();  // 构建最终的客户端配置
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
     * 用户认证安全过滤器链 - 优先级为1
     * 处理登录、注册相关的所有请求和静态资源
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authenficationSecurityFilterChain(
            HttpSecurity http,
            UserDetailsService userDetailsService,
            AuthCommonServiceImpl authCommonService) throws Exception {
        log.info("配置用户认证安全过滤器链");
        // 1. 获取 AuthenticationManager
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        // 注册我们的 Provider
        SmsAuthenticationProvider smsProvider = new SmsAuthenticationProvider(userDetailsService, authCommonService);
        UsernamePasswordAuthenticationProvider usernamePasswordProvider =
                new UsernamePasswordAuthenticationProvider(userDetailsService, bCryptPasswordEncoder);
        authenticationManagerBuilder.authenticationProvider(smsProvider);
        authenticationManagerBuilder.authenticationProvider(usernamePasswordProvider);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        // 2. 配置 Filter
        SmsAuthenticationFilter smsFilter = new SmsAuthenticationFilter(authenticationManager);
        MyUsernamePasswordAuthenticationFilter usernamePasswordFilter =
                new MyUsernamePasswordAuthenticationFilter(authenticationManager);

        // 登录成功后，重定向回之前的请求（例如 /oauth2/authorize）
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setRequestCache(getHttpSessionRequestCache());
        // 设置总是使用保存的请求URL（如果有）
        successHandler.setAlwaysUseDefaultTargetUrl(false);

        // 配置短信认证过滤器
        smsFilter.setAuthenticationSuccessHandler(successHandler);
        smsFilter.setSecurityContextRepository(securityContextRepository());
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/login");
        smsFilter.setAuthenticationFailureHandler(failureHandler);

        // 配置用户名密码过滤器（现在同时支持表单和JSON提交）
        usernamePasswordFilter.setAuthenticationSuccessHandler(successHandler);
        usernamePasswordFilter.setSecurityContextRepository(securityContextRepository());
        usernamePasswordFilter.setAuthenticationFailureHandler(failureHandler);
        http
                .securityMatcher(
                        "/",
                        "/user-agreement",
                        "/privacy-policy",
                        "/disclaimer",
                        "/register-info",
                        "/login",
                        "/login/**",
                        "/register",
                        "/register/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/favicon.ico",
                        "/static/**"
                )
                .securityContext(securityContext ->
                        securityContext.securityContextRepository(securityContextRepository()))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        ))
                // 将我们的 Manager 重新设置回去
                .authenticationManager(authenticationManager)
                // ★ 把短信 Filter 加在用户名密码 Filter 之前
                .addFilterBefore(smsFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加我们自定义的用户名密码过滤器（同时支持表单和JSON提交）
                .addFilterBefore(usernamePasswordFilter, UsernamePasswordAuthenticationFilter.class)
                // 启用CSRF保护，Thymeleaf会自动处理CSRF token
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .cors(AbstractHttpConfigurer::disable)
                .headers(CommonSecurityConfigUtil::getPermissionsPolicyConfig);

        return http.build();
    }

    @Bean
    public HttpSessionRequestCache getHttpSessionRequestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        // 可选：关闭 ?continue 参数
        requestCache.setMatchingRequestParameterName(null);
        // 关键：配置它只保存 OAuth2 请求，忽略静态资源和登录页
        requestCache.setRequestMatcher(request -> {
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();

            log.debug("RequestCache检查请求 - URI: {}, QueryString: {}", uri, queryString);

            // 如果是静态资源或登录相关，都不保存
            if (uri.matches(".*\\.(css|js|jpg|png|gif|ico|woff|woff2|ttf|map)$")) return false;

            // 保存OAuth2授权请求和重要的业务页面
            if (uri.startsWith("/oauth2/authorize")) {
                log.info("保存OAuth2授权请求: {}?{}", uri, queryString);
                return true;
            }

            // 排除登录注册相关的请求
            if (uri.startsWith("/login") || uri.startsWith("/register")) return false;

            // 保存其他可能包含OAuth2参数的请求
            if (queryString != null && (queryString.contains("client_id") || queryString.contains("redirect_uri"))) {
                log.info("保存包含OAuth2参数的请求: {}?{}", uri, queryString);
                return true;
            }

            return false;
        });
        return requestCache;
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
            OAuth2AuthorizationService oAuth2AuthorizationService
    ) throws Exception {

        log.info("配置授权服务器安全过滤器链");
        // 创建授权服务器配置器，用于自定义OAuth2端点行为
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer.oidc(Customizer.withDefaults());
        return http
                // 设置安全匹配器，只处理授权服务器端点请求
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
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
                .requestCache(c -> c.requestCache(getHttpSessionRequestCache()))
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
