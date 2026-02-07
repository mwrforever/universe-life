package com.universe.life.pay.infrastructure.alipay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "pay.alipay")
public class AlipayProperties {

    private boolean enabled;

    private String serverUrl;

    private String appId;

    private String merchantPrivateKey;

    private String alipayPublicKey;

    private String notifyUrl;

    private String charset = "utf-8";

    private String signType = "RSA2";
}
