package com.universe.life.pay.infrastructure.wechat;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

@Configuration
@EnableConfigurationProperties(WechatPayProperties.class)
public class WechatPayConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "pay.wechat", name = "enabled", havingValue = "true")
    public PrivateKey wechatPayMerchantPrivateKey(WechatPayProperties properties, ResourceLoader resourceLoader) throws Exception {
        Resource resource = resourceLoader.getResource(properties.getPrivateKeyPath());
        try (InputStream in = resource.getInputStream()) {
            return PemUtil.loadPrivateKey(in);
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "pay.wechat", name = "enabled", havingValue = "true")
    public Verifier wechatPayVerifier(WechatPayProperties properties, PrivateKey wechatPayMerchantPrivateKey) {
        PrivateKeySigner signer = new PrivateKeySigner(properties.getMchSerialNo(), wechatPayMerchantPrivateKey);
        WechatPay2Credentials credentials = new WechatPay2Credentials(properties.getMchId(), signer);
        return new AutoUpdateCertificatesVerifier(credentials, properties.getApiV3Key().getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    @ConditionalOnProperty(prefix = "pay.wechat", name = "enabled", havingValue = "true")
    public CloseableHttpClient wechatPayHttpClient(WechatPayProperties properties,
                                                  PrivateKey wechatPayMerchantPrivateKey,
                                                  Verifier wechatPayVerifier) {
        return WechatPayHttpClientBuilder.create()
                .withMerchant(properties.getMchId(), properties.getMchSerialNo(), wechatPayMerchantPrivateKey)
                .withValidator(new WechatPay2Validator(wechatPayVerifier))
                .build();
    }
}
