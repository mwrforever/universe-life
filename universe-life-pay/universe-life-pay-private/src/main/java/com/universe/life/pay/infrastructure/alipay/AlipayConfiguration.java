package com.universe.life.pay.infrastructure.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "pay.alipay", name = "enabled", havingValue = "true")
    public AlipayClient alipayClient(AlipayProperties properties) {
        return new DefaultAlipayClient(
                properties.getServerUrl(),
                properties.getAppId(),
                properties.getMerchantPrivateKey(),
                "json",
                properties.getCharset(),
                properties.getAlipayPublicKey(),
                properties.getSignType()
        );
    }
}
