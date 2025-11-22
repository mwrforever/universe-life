package com.universe.life.auth.service.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 毛伟然
 * @since 2025/11/19 15:51
 */
@Data
@ConfigurationProperties(prefix = "universe-life.jwk")
public class JwkProperties {

    private Integer primaryCount = 1;

    private String defaultAlgorithm = "RS256";

}
