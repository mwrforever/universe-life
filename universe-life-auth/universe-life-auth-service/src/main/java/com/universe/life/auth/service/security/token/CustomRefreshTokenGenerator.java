package com.universe.life.auth.service.security.token; // 放在你项目合适的包下

import org.springframework.lang.Nullable;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.time.Instant;
import java.util.Base64;

/**
 * 自定义 Refresh Token 生成器
 * 暴力破解：允许 Public Client 获取 Refresh Token
 */
public final class CustomRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {

    private final StringKeyGenerator refreshTokenGenerator = new Base64StringKeyGenerator(
            Base64.getUrlEncoder().withoutPadding(), 96);

    @Nullable
    @Override
    public OAuth2RefreshToken generate(OAuth2TokenContext context) {
        // 1. 只处理 Refresh Token
        if (!OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
            return null;
        }

        // 2. 检查有效期配置
        if (context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive().isZero()) {
            return null;
        }
        // 3. 生成 Token
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive());

        return new OAuth2RefreshToken(this.refreshTokenGenerator.generateKey(), issuedAt, expiresAt);
    }
}