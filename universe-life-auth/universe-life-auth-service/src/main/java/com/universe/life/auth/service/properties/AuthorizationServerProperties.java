package com.universe.life.auth.service.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author 毛伟然
 * @since 2025/11/12 10:19
 */
@Data
@ConfigurationProperties(prefix = "universe-life.security.authorization.server")
public class AuthorizationServerProperties {

    private String issuer;

    private String clientId;

    private String clientSecret;

    private List<String> redirectUri;

    private String postLogoutRedirectUri;

    private String consentPage;

}
