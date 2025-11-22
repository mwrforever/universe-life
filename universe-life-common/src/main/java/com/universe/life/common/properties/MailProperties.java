package com.universe.life.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 毛伟然
 * @since 2025/11/17 12:19
 */
@Data
@ConfigurationProperties("universe-life.mail")
public class MailProperties {


    private String from;

    private String appName;

    private String appUrl;

}
