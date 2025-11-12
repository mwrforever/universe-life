package com.universe.life.auth.service.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

    private String redirectUri;

    private String consentPage;

}
