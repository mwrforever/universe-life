package com.universe.life.auth.service.security.convert; // 放在你合适的包下

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * 专门用于 Revoke 端点的公共客户端转换器
 * 不校验 PKCE (code_verifier)，只校验 client_id 存在且无 secret
 */
public class PublicClientRevocationAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 获取 client_id
        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);

        if (!StringUtils.hasText(clientId)) {
            return null; // 不归我管，或者格式不对
        }

        return new OAuth2ClientAuthenticationToken(
                clientId,
                ClientAuthenticationMethod.NONE,
                null,
                null
        );
    }
}