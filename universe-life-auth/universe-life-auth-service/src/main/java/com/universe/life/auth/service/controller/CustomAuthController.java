package com.universe.life.auth.service.controller;

import com.alibaba.nacos.api.model.v2.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 毛伟然
 * @since 2025/12/9 17:27
 */
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "认证接口", description = "认证接口")
@Slf4j
public class CustomAuthController {

    private final OAuth2AuthorizationService authorizationService;

    /**
     * 自定义撤销令牌接口
     * 完全绕过 Spring Security 的 Client 认证过滤器，由自己控制逻辑
     */
    @PostMapping("/oauth2/revoke")
    public Result<String> revokeToken(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
            @RequestParam(value = "client_id", required = false) String clientId) {

        log.info("接收到自定义撤销请求 - ClientID: {}, TokenHint: {}", clientId, tokenTypeHint);

        // 1. 尝试根据 Token 查找授权信息
        // 先试着按 Access Token 找
        OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization == null) {
            // 再试着按 Refresh Token 找
            authorization = authorizationService.findByToken(token, OAuth2TokenType.REFRESH_TOKEN);
        }

        // 2. 如果没找到，根据 RFC 7009 标准，仍然返回 200 (意味着"撤销成功"，即便它本来就不存在)
        if (authorization == null) {
            log.warn("未找到对应的 Token，直接返回成功");
            return Result.success("Revocation successful (Token not found)");
        }

        // 3. (可选) 校验 Client ID，防止恶意撤销别人的 Token（如果是公共端点，这步很重要）
        // 如果数据库里的 registeredClientId 和传进来的不一致，可以拒绝
        if (clientId != null && !clientId.equals(authorization.getRegisteredClientId())) {
            log.error("Client ID 不匹配，拒绝撤销。Token所属: {}, 请求ID: {}",
                    authorization.getRegisteredClientId(), clientId);
            return Result.failure("Invalid Client ID");
        }

        // 4. 执行作废逻辑
        // 由于 OAuth2Authorization 是不可变对象，我们需要用 Builder 重新构建
        OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);

        // 如果是 Access Token
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken != null && accessToken.getToken().getTokenValue().equals(token)) {
            // 标记 Access Token 为无效
            builder.token(accessToken.getToken(), metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, "invalidated"));
            log.info("Access Token 已作废");
        }

        // 如果是 Refresh Token
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        if (refreshToken != null && refreshToken.getToken().getTokenValue().equals(token)) {
            // 标记 Refresh Token 为无效
            builder.token(refreshToken.getToken(), metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, "invalidated"));
            log.info("Refresh Token 已作废");
        }

        // 或者，如果你想直接彻底删除这条授权记录（更彻底，用户必须重新登录）
        // authorizationService.remove(authorization);
        // 推荐：上面的 builder 方式只是标记，下面这一行是彻底移除，看你业务需求
        authorizationService.remove(authorization);
        log.info("授权记录已移除");

        return Result.success("Revocation successful");
    }

    /**
     * 自定义退出登录接口
     */
    @PostMapping("/connect/logout")
    public Result<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            log.info("用户退出登录: {}", authentication.getName());
        }

        // 1. 调用 Spring Security 标准的退出处理器
        // 这会清除 SecurityContext，销毁 HttpSession，清理 RememberMe 等
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);

        return Result.success("Logout successful");
    }

}
