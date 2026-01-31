package com.universe.life.auth.service.security.config;

import com.universe.life.auth.resource.util.CommonSecurityConfigUtil;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.auth.service.security.filter.MyUsernamePasswordAuthenticationFilter;
import com.universe.life.auth.service.security.filter.SmsAuthenticationFilter;
import com.universe.life.auth.service.security.handler.JsonAuthenticationFailureHandler;
import com.universe.life.auth.service.security.provider.SmsAuthenticationProvider;
import com.universe.life.auth.service.security.provider.UsernamePasswordAuthenticationProvider;
import com.universe.life.auth.service.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import java.util.Map;

/**
 * @author 毛伟然
 * @since 2025/12/21 14:05
 */
@Slf4j
@Configuration
public class LoginSecurityConfiguration {

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


    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * AuthenticationManager Bean - 用于用户认证
     * 将其暴露为 Bean 以便在其他服务中注入使用
     */
    @Bean
    public AuthenticationManager loginAuthenticationManager(
            Map<String, UserDetailsService> userAuthInfoServices,
            PasswordEncoder bCryptPasswordEncoder,
            AuthUtil authUtil,
            VerifyCaptchaUtil verifyCaptchaUtil) {
        // 注册自定义 Provider
        SmsAuthenticationProvider smsProvider = new SmsAuthenticationProvider(userAuthInfoServices, verifyCaptchaUtil, authUtil);
        UsernamePasswordAuthenticationProvider usernamePasswordProvider =
                new UsernamePasswordAuthenticationProvider(userAuthInfoServices, bCryptPasswordEncoder, authUtil);


        return new ProviderManager(smsProvider, usernamePasswordProvider);
    }

    /**
     * 用户认证安全过滤器链 - 优先级为1
     * 处理登录、注册相关的所有请求和静态资源
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authenficationSecurityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            JsonAuthenticationFailureHandler authenticationFailureHandler,
            AuthUtil authUtil
    ) throws Exception {
        log.info("配置用户认证安全过滤器链");

        // 配置 Filter
        SmsAuthenticationFilter smsFilter = new SmsAuthenticationFilter(authenticationManager, authUtil);
        MyUsernamePasswordAuthenticationFilter usernamePasswordFilter =
                new MyUsernamePasswordAuthenticationFilter(authenticationManager, authUtil);

        // 登录成功后，重定向回之前的请求（例如 /oauth2/authorize）
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setRequestCache(getHttpSessionRequestCache());
        // 设置总是使用保存的请求URL（如果有）
        successHandler.setAlwaysUseDefaultTargetUrl(false);

        // 配置短信认证过滤器
        smsFilter.setAuthenticationSuccessHandler(successHandler);
        smsFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        smsFilter.setSecurityContextRepository(securityContextRepository());

        // 配置用户名密码过滤器（现在同时支持表单和JSON提交）
        usernamePasswordFilter.setAuthenticationSuccessHandler(successHandler);
        usernamePasswordFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        usernamePasswordFilter.setSecurityContextRepository(securityContextRepository());
        http
                .securityMatcher(
                        "/", "/user-agreement", "/privacy-policy", "/disclaimer", "/register-info"
                        , "/login/**", "/register", "/register/**", "/css/**", "/js/**", "/img/**",
                        "/favicon.ico", "/static/**"

                )
                .securityContext(securityContext ->
                        securityContext.securityContextRepository(securityContextRepository()))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                // 将我们的 Manager 重新设置回去
                .authenticationManager(authenticationManager)
                // ★ 把短信 Filter 加在用户名密码 Filter 之前
                .addFilterBefore(smsFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加我们自定义的用户名密码过滤器（同时支持表单和JSON提交）
                .addFilterBefore(usernamePasswordFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(AbstractHttpConfigurer::disable)
                .headers(CommonSecurityConfigUtil::getPermissionsPolicyConfig);

        return http.build();
    }

}
