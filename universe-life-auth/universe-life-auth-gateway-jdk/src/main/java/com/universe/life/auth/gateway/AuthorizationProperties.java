package com.universe.life.auth.gateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 毛伟然
 * @since 2025/11/14 14:17
 */
@Data
@ConfigurationProperties(prefix = "universe-life.security.resource.server")
public class AuthorizationProperties {

    private String issuerUri;

}
