package com.universe.life.auth.service.security;

import com.universe.life.auth.common.constants.JwtConstants;
import com.universe.life.auth.common.domain.dto.AdminAuthInfo;
import com.universe.life.auth.common.domain.dto.UserAuthInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * @author 毛伟然
 * @since 2025/11/24 14:03
 */
@Slf4j
public class TokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {

        // 1. 处理 Access Token
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            handleAccessToken(context);
        }
        // 2. 处理 ID Token
        else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            handleIdToken(context);
        }
    }

    private void handleAccessToken(JwtEncodingContext context) {
        Authentication authentication = context.getPrincipal();

        JwtClaimsSet.Builder claims = context.getClaims();
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
            claims.claim(JwtConstants.CLIENT_ID, authentication.getName());
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserAuthInfo user) {
            claims.claim(JwtConstants.USER_ID, user.getId());
        } else if (principal instanceof AdminAuthInfo adminInfo) {
            claims.claim(JwtConstants.USER_ID, adminInfo.getId());
        } else {
            log.warn("Access Token Principal mismatch: {}", principal.getClass().getName());
        }
    }

    private void handleIdToken(JwtEncodingContext context) {
        Authentication authentication = context.getPrincipal();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserAuthInfo user) {
            // ID Token 中通常放入用于展示的用户信息
            context.getClaims().claim(JwtConstants.USER_NAME, user.getUsername());
            context.getClaims().claim(JwtConstants.USER_AVATAR, user.getAvatar() == null ? "https://abc.png" : user.getAvatar());
        } else if (principal instanceof AdminAuthInfo adminInfo
        ) {
            // ID Token 中通常放入用于展示的用户信息
            context.getClaims().claim(JwtConstants.USER_NAME, adminInfo
                    .getUsername());
            context.getClaims().claim(JwtConstants.USER_AVATAR, adminInfo
                    .getAvatar() == null ? "https://abc.png" : adminInfo
                    .getAvatar());
        } else {
            log.warn("ID Token Principal mismatch: {}", principal.getClass().getName());
        }
    }
}