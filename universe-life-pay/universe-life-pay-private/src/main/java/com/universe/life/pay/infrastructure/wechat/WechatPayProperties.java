package com.universe.life.pay.infrastructure.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "pay.wechat")
public class WechatPayProperties {

    private boolean enabled;

    private String appId;

    private String mchId;

    private String mchSerialNo;

    private String apiV3Key;

    private String privateKeyPath;

    private String notifyUrl;
}
