package com.universe.life.auth.resource.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * @author 毛伟然
 * @since 2025/11/1 17:28
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@ConfigurationProperties("universe-life.auth")
public class AuthPathProperties {

    private Set<String> excludePath;

    private Boolean enable;
}
