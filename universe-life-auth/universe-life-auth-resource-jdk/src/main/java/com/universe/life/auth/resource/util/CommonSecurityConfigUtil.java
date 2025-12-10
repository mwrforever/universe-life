package com.universe.life.auth.resource.util;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * @author 毛伟然
 * @since 2025/12/9 11:37
 */
public class CommonSecurityConfigUtil {
    public static HeadersConfigurer<HttpSecurity>.PermissionsPolicyConfig getPermissionsPolicyConfig(HeadersConfigurer<HttpSecurity> headers) {
        return headers
                // 1. 内容安全策略 (CSP) - 防止XSS攻击
                // 开发建议：
                // - script-src 添加 'unsafe-inline' 'unsafe-eval' 以允许Vue/React的热重载脚本和内联样式
                // - connect-src 添加 'https:' 'http:' 以允许前端连接后端API接口（前后端分离常见跨域）
                // - img-src 添加 'data:' 以允许Base64图片
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data: https: http:; " +
                                "font-src 'self' data:; " +
                                "connect-src 'self' https: http:;" +  // 允许连接外部API
                                "connect-src 'self' *"
                        )
                )

                // 2. 跨域嵌入保护 (Frame Options) - 防止点击劫持
                // 建议改为 sameOrigin，否则 H2 Console、Swagger UI 等内嵌页面在开发时无法打开
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)

                // 3. 传输安全 (HSTS) - 强制客户端使用HTTPS
                // 注意：这仅在通过HTTPS访问时生效。在本地 HTTP 开发环境中通常会被忽略，但在生产环境至关重要。
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)   // 包含子域名
                        .maxAgeInSeconds(31536000) // 有效期一年
                )

                // 4. 内容类型选项 - 防止MIME类型嗅探 (X-Content-Type-Options: nosniff)
                // 强制浏览器使用服务器声明的 Content-Type，防止伪装文件攻击
                .contentTypeOptions(Customizer.withDefaults())

                // 5. XSS保护 (X-XSS-Protection) - 启用浏览器XSS过滤器
                // 注意：这是一个旧的头部，现代浏览器主要依赖 CSP。
                // 这里设置为 "1; mode=block"，即检测到攻击时阻止页面加载
                .xssProtection(xss -> xss
                        .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )

                // 6. 引用策略 - 防止敏感信息通过 Referer 头部泄露
                // STRICT_ORIGIN_WHEN_CROSS_ORIGIN 是兼顾安全与统计的较好选择
                // (同源请求发送完整URL，跨域请求只发送域名，HTTPS->HTTP 不发送)
                .referrerPolicy(referrer -> referrer
                        .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )

                // 7. 权限策略 (Permissions Policy) - 控制浏览器特性访问
                // 禁用不必要的硬件接口，减少攻击面
                .permissionsPolicy(permissions -> permissions
                        .policy("geolocation=(), microphone=(), camera=(), fullscreen=()")
                );
    }

}
