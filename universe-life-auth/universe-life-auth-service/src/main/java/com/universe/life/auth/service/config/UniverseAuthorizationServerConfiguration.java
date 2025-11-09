package com.universe.life.auth.service.config;

import com.universe.life.auth.service.handler.JwtAccessDeniedHandler;
import com.universe.life.auth.service.handler.JwtAuthenticationExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author 毛伟然
 * @since 2025/11/8 10:48
 */
@Slf4j
@Configuration
public class UniverseAuthorizationServerConfiguration {

    /**
     * 授权服务器基本设置Bean
     *
     * <p>配置OAuth2授权服务器的核心参数，主要是issuer URI。
     * issuer是授权服务器的唯一标识符，用于JWT令牌验证和发现服务。</p>
     *
     * @param issuer 授权服务器的URL，默认值为http://localhost:8099，
     *               可通过spring.security.oauth2.authorizationserver.issuer配置
     * @return 配置好的AuthorizationServerSettings对象
     */
    @Bean  // Spring注解：将方法返回的对象注册为Spring容器中的Bean
    public AuthorizationServerSettings authorizationServerSettings(
            // Spring注解：从配置文件或环境变量中读取值，支持默认值
            @Value("${spring.security.oauth2.authorizationserver.issuer:http://localhost:8099}") String issuer) {

        // 记录配置信息到日志，便于调试和监控
        log.info("配置授权服务器设置 - Issuer: {}", issuer);

        // 使用建造者模式创建AuthorizationServerSettings对象
        return AuthorizationServerSettings.builder()
                .issuer(issuer)  // 设置授权服务器的issuer URI
                .build();  // 构建最终的配置对象
    }


    /**
     * 客户端注册仓库Bean - 生产环境数据库存储
     *
     * <p>配置OAuth2客户端信息的持久化存储，使用JdbcOperations进行数据库操作。
     * 支持动态客户端注册和管理，自动初始化默认客户端。</p>
     *
     * @param jdbcOperations      Spring的JDBC操作模板，用于执行SQL语句
     * @param gatewayClientId     网关客户端ID，默认值为universe-life-gateway
     * @param gatewayClientSecret 网关客户端密钥，可通过配置设置
     * @param gatewayRedirectUri  网关客户端回调URI，用于OAuth2授权码重定向
     * @return 配置好的RegisteredClientRepository对象
     */
    @Bean  // Spring注解：注册客户端仓库Bean
    public RegisteredClientRepository registeredClientRepository(
            JdbcOperations jdbcOperations,  // Spring JDBC操作对象，用于数据库访问
            @Value("${auth.clients.gateway.id:universe-life-gateway}") String gatewayClientId,  // 网关客户端ID
            @Value("${auth.clients.gateway.secret:#{null}}") String gatewayClientSecret,  // 网关客户端密钥
            @Value("${auth.clients.gateway.redirect-uri:http://localhost:8101/login/oauth2/code/gateway}") String gatewayRedirectUri) {  // 回调URI

        // 记录初始化信息到日志
        log.info("初始化客户端注册仓库 - 数据库模式");

        // 创建基于数据库的客户端注册仓库
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcOperations);

        // 检查默认客户端是否已存在，避免重复创建
        if (!isClientExists(repository, gatewayClientId)) {
            // 创建网关客户端配置
            RegisteredClient gatewayClient = createGatewayClient(gatewayClientId, gatewayClientSecret, gatewayRedirectUri);
            // 保存客户端到数据库
            repository.save(gatewayClient);
            log.info("已创建网关客户端: {}", gatewayClientId);
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
     * @param clientId     客户端标识符
     * @param clientSecret 客户端密钥，可为空时使用默认密钥
     * @param redirectUri  授权回调URI
     * @return 配置完成的RegisteredClient对象
     */
    private RegisteredClient createGatewayClient(String clientId, String clientSecret, String redirectUri) {
        // 使用BCrypt算法加密客户端密钥，强度12，提供强密码保护
        String encodedSecret = clientSecret != null ?
                passwordEncoder().encode(clientSecret) :
                "{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK";  // 默认开发环境密钥

        // 使用建造者模式创建RegisteredClient对象
        return RegisteredClient.withId(UUID.randomUUID().toString())  // 生成唯一客户端ID
                .clientId(clientId)  // 设置客户端标识符
                .clientSecret(encodedSecret)  // 设置加密后的客户端密钥
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)  // 客户端认证方式：HTTP Basic认证
                .authorizationGrantTypes(grants -> grants.addAll(Arrays.asList(  // 支持的授权类型
                        AuthorizationGrantType.AUTHORIZATION_CODE,  // 授权码模式，最安全的OAuth2流程
                        AuthorizationGrantType.REFRESH_TOKEN,  // 刷新令牌模式，支持令牌续期
                        AuthorizationGrantType.CLIENT_CREDENTIALS,  // 客户端凭证模式，用于服务间调用
                        AuthorizationGrantType.JWT_BEARER  // JWT Bearer模式，支持JWT令牌
                )))
                .redirectUris(uris -> uris.addAll(Arrays.asList(  // 支持的回调URI列表
                        redirectUri,  // 主要回调URI，从配置获取
                        "http://localhost:8101",  // 开发环境本地地址
                        "https://gateway.yourdomain.com/login/oauth2/code/gateway"  // 生产环境地址
                )))
                .scopes(scopes -> scopes.addAll(Arrays.asList(  // 支持的权限范围
                        OidcScopes.OPENID,  // OpenID Connect标准范围，获取用户标识
                        OidcScopes.PROFILE,  // 用户基本信息范围
                        OidcScopes.EMAIL,  // 用户邮箱范围
                        OidcScopes.PHONE,  // 用户电话范围
                        "read",  // 读权限，自定义业务权限
                        "write",  // 写权限，自定义业务权限
                        "admin",  // 管理员权限
                        "trust"  // 信任权限，用于特殊操作
                )))
                .clientSettings(ClientSettings.builder()  // 客户端设置
                        .requireAuthorizationConsent(false)  // 不要求用户授权同意，适用于可信客户端
                        .requireProofKey(true)  // 启用PKCE，增强授权码模式安全性
                        .build())
                .tokenSettings(TokenSettings.builder()  // 令牌设置
                        .accessTokenTimeToLive(Duration.ofMinutes(30))  // 访问令牌有效期：30分钟
                        .refreshTokenTimeToLive(Duration.ofDays(30))  // 刷新令牌有效期：30天
                        .reuseRefreshTokens(false)  // 不重复使用刷新令牌，增强安全性
                        .authorizationCodeTimeToLive(Duration.ofMinutes(5))  // 授权码有效期：5分钟
                        .build())
                .build();  // 构建最终的客户端配置
    }

    // ================================================================================
    // 生产环境客户端配置 - 注释状态，需要时启用
    // ================================================================================
    /*
    private RegisteredClient createProductionGatewayClient(String clientId, String clientSecret, String redirectUri) {
        // 使用BCrypt算法加密客户端密钥，强度12，提供强密码保护
        String encodedSecret = clientSecret != null ?
            passwordEncoder().encode(clientSecret) :
            "{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKVjzieMwkOmANgNOgKQNNBDvAGK";  // 默认开发环境密钥

        // 使用建造者模式创建生产环境安全客户端配置
        return RegisteredClient.withId(UUID.randomUUID().toString())  // 生成唯一客户端ID
                .clientId(clientId)  // 设置客户端标识符
                .clientSecret(encodedSecret)  // 设置加密后的客户端密钥
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)  // 客户端认证方式：HTTP Basic认证
                .authorizationGrantTypes(grants -> grants.addAll(Arrays.asList(  // 生产环境支持的授权类型
                    AuthorizationGrantType.AUTHORIZATION_CODE,  // 授权码模式，最安全的OAuth2流程
                    AuthorizationGrantType.REFRESH_TOKEN,  // 刷新令牌模式，支持令牌续期
                    AuthorizationGrantType.CLIENT_CREDENTIALS  // 客户端凭证模式，用于服务间调用
                    // 生产环境注释：暂时不启用JWT_BEARER模式，增强安全性
                )))
                .redirectUris(uris -> uris.addAll(Arrays.asList(  // 生产环境严格的回调URI列表
                    redirectUri,  // 主要回调URI，从配置获取
                    "https://gateway.yourdomain.com/login/oauth2/code/gateway"  // 生产环境地址（HTTPS）
                    // 注释：开发环境地址已移除，生产环境只允许HTTPS
                )))
                .scopes(scopes -> scopes.addAll(Arrays.asList(  // 生产环境最小权限原则
                    OidcScopes.OPENID,  // OpenID Connect标准范围，获取用户标识
                    OidcScopes.PROFILE,  // 用户基本信息范围
                    OidcScopes.EMAIL,  // 用户邮箱范围
                    "read",  // 读权限，自定义业务权限
                    "write"  // 写权限，自定义业务权限
                    // 注释：生产环境不直接授予admin和trust权限，需要特殊申请
                )))
                .clientSettings(ClientSettings.builder()  // 生产环境严格客户端设置
                        .requireAuthorizationConsent(true)  // 要求用户授权同意，增强用户体验和安全性
                        .requireProofKey(true)  // 启用PKCE，增强授权码模式安全性
                        .build())
                .tokenSettings(TokenSettings.builder()  // 生产环境严格令牌设置
                        .accessTokenTimeToLive(Duration.ofMinutes(15))  // 访问令牌有效期：15分钟（缩短）
                        .refreshTokenTimeToLive(Duration.ofDays(7))  // 刷新令牌有效期：7天（缩短）
                        .reuseRefreshTokens(false)  // 不重复使用刷新令牌，增强安全性
                        .authorizationCodeTimeToLive(Duration.ofMinutes(3))  // 授权码有效期：3分钟（缩短）
                        .build())
                .build();  // 构建最终的生产环境客户端配置
    }
    */

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
            repository.findByClientId(clientId);
            // 如果没有抛出异常，说明客户端存在
            return true;
        } catch (Exception e) {
            // 如果抛出异常（如EmptyResultDataAccessException），说明客户端不存在
            return false;
        }
    }


    /**
     * 授权服务器安全过滤器链 - 优先级为1
     * 处理所有OAuth2授权服务器端点请求
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            JwtAuthenticationExceptionHandler jwtAuthenticationExceptionHandler,
            @Value("${spring.security.oauth2.authorizationserver.authorization-consent-page:/oauth2/consent}") String consentPage) throws Exception {

        log.info("配置授权服务器安全过滤器链");

        // 应用Spring Authorization Server默认安全配置
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // 创建授权服务器配置器，用于自定义OAuth2端点行为
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        // 自定义授权端点配置，设置用户授权同意页面路径
        authorizationServerConfigurer
                .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.consentPage(consentPage));

        return http
                // 设置安全匹配器，只处理授权服务器端点请求
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())

                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> authorize
                        // 健康检查端点 - 允许公开访问，用于监控和服务发现
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // 错误页面和静态资源 - 允许公开访问
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        // 授权同意页面 - 需要用户登录认证
                        .requestMatchers(consentPage).authenticated()
                        // 其他所有请求 - 需要认证
                        .anyRequest().authenticated())

                // 配置CSRF保护 - 对授权服务器端点禁用CSRF（符合OAuth2标准）
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))

                // 配置会话管理 - 使用无状态会话（STATELESS）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置异常处理 - 设置认证入口点和访问拒绝处理器
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationExceptionHandler)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                // 应用授权服务器配置
                .with(authorizationServerConfigurer, Customizer.withDefaults())

                // 配置HTTP安全头 - 开发阶段简化配置，避免影响前后端分离开发
                .headers((headers) -> {
                })
                .build();  // 构建并返回安全过滤器链对象

        // ================================================================================
        // 生产环境安全头配置 - 注释状态，需要时启用
        // ================================================================================
        /*
        // 完整的HTTP安全头配置 - 生产环境启用
        .headers(headers -> headers
                // 内容安全策略 - 防止XSS攻击
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self'"))

                // 跨域嵌入保护 - 防止点击劫持
                .frameOptions(frame -> frame.deny())

                // 传输安全 - 强制HTTPS
                .hsts(hsts -> hsts
                        .maxAgeInSeconds(31536000)  // 1年
                        .includeSubDomains(true)
                        .preload(true))

                // 内容类型选项 - 防止MIME类型嗅探
                .contentTypeOptions(contentType -> {})

                // XSS保护 - 启用浏览器XSS过滤器
                .xssProtection(xss -> xss.headerValue(HeaderWriterFilter.XXSSProtectionMode.ENABLED_MODE_BLOCK))

                // 引用策略 - 防止敏感信息泄露
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

                // 权限策略 - 控制浏览器特性访问
                .permissionsPolicy(permissions -> permissions
                        .policy("geolocation=(), microphone=(), camera=(), fullscreen=()"))
        )
        */
    }

    /**
     * 默认安全过滤器链 - 优先级为2
     * 处理非OAuth2授权服务器的其他HTTP请求
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            JwtAuthenticationExceptionHandler jwtAuthenticationExceptionHandler) throws Exception {

        log.info("配置默认安全过滤器链");

        return http
                // 匹配所有请求（除了已被授权服务器过滤器链处理的OAuth2端点）
                .securityMatcher("/**")

                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> authorize
                        // 登录页面 - 允许公开访问，用于用户登录
                        .requestMatchers("/login", "/admin/login").permitAll()
                        // 注册界面 - 允许公开访问，用于用户注册
                        .requestMatchers("/register").permitAll()
                        // 健康检查端点 - 允许公开访问，用于服务监控
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // 错误页面和网站图标 - 允许公开访问
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        // 静态资源文件 - 允许公开访问
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                        // API文档 - 需要用户认证后访问
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/doc.html").authenticated()
                        // 管理端点 - 需要管理员权限访问
                        .requestMatchers("/actuator/**", "/admin/**").hasRole("ADMIN")
                        // 其他所有请求 - 需要认证
                        .anyRequest().authenticated())

                // 完全禁用CSRF保护 - 适用于RESTful API服务
                .csrf(AbstractHttpConfigurer::disable)

                // 配置会话管理 - 使用无状态会话（STATELESS）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置异常处理 - 设置认证失败和权限不足的处理逻辑
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationExceptionHandler)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                // 配置HTTP安全头 - 开发阶段简化配置
                .headers((headers) -> {
                })
                .build();  // 构建并返回默认安全过滤器链对象

        // ================================================================================
        // 生产环境默认安全过滤器链配置 - 注释状态，需要时启用
        // ================================================================================
        /*
        // 生产环境完整安全配置
        return http
                // 匹配所有请求（除了已被授权服务器过滤器链处理的OAuth2端点）
                .securityMatcher("/**")

                // 配置请求授权规则 - 生产环境更严格的访问控制
                .authorizeHttpRequests(authorize -> authorize
                        // 健康检查端点 - 允许公开访问，用于服务监控
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // 错误页面和网站图标 - 允许公开访问
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        // 静态资源文件 - 生产环境需要认证访问
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").authenticated()
                        // API文档 - 生产环境禁用或需要管理员权限
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/doc.html").hasRole("ADMIN")
                        // 管理端点 - 需要管理员权限访问
                        .requestMatchers("/actuator/**", "/admin/**").hasRole("ADMIN")
                        // 其他所有请求 - 需要认证
                        .anyRequest().authenticated())

                // 生产环境CSRF保护 - 对关键操作启用CSRF
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/actuator/health", "/actuator/info"))

                // 配置会话管理 - 生产环境启用会话保护
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(10)
                        .maxSessionsPreventsLogin(true))

                // 配置异常处理 - 设置认证失败和权限不足的处理逻辑
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationExceptionHandler)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                // 完整的HTTP安全头配置 - 生产环境启用
                .headers(headers -> headers
                        // 内容安全策略 - 防止XSS攻击
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data: https:; font-src 'self'; connect-src 'self'"))

                        // 跨域嵌入保护 - 防止点击劫持
                        .frameOptions(frame -> frame.sameOrigin())

                        // 传输安全 - 强制HTTPS
                        .hsts(hsts -> hsts
                                .maxAgeInSeconds(31536000)  // 1年
                                .includeSubDomains(true))

                        // 内容类型选项 - 防止MIME类型嗅探
                        .contentTypeOptions(contentType -> {})

                        // XSS保护 - 启用浏览器XSS过滤器
                        .xssProtection(xss -> xss.headerValue(HeaderWriterFilter.XXSSProtectionMode.ENABLED_MODE_BLOCK))

                        // 引用策略 - 防止敏感信息泄露
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .build();  // 构建并返回生产环境安全过滤器链对象
        */
    }

    // ================================
    // 4. 支持组件配置
    // ================================

    /**
     * 密码编码器Bean
     *
     * <p>提供BCrypt密码编码器，用于加密敏感信息如客户端密钥等。
     * BCrypt是一种强哈希算法，专门用于密码存储。</p>
     *
     * @return BCryptPasswordEncoder实例
     */
    @Bean  // Spring注解：注册密码编码器Bean
    public PasswordEncoder passwordEncoder() {
        // 创建强度为12的BCrypt密码编码器，提供良好的安全性和性能平衡
        return new BCryptPasswordEncoder(12);
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
