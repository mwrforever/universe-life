package com.universe.life.auth.service.manager;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 毛伟然
 * @since 2025/11/5 09:34
 */
@RequiredArgsConstructor
@Configuration
@Slf4j
public class JwkManager {

    private final ConcurrentHashMap<String, KeyPair> store = new ConcurrentHashMap<>();

    private final AtomicReference<String> primary = new AtomicReference<>();


    @PostConstruct
    public void init() {
        rotate();
    }

    /**
     * 密钥轮换：
     * 1. 生成新 KeyPair
     * 2. 设置为主用
     * 3. 旧密钥可保留 1~2 个 TTL，用于已发出 Token 的验签
     */
    public void rotate() {
        // 先清除旧的密钥
        String newId = UUID.randomUUID().toString();
        KeyPair pair = generateRsaKey();
        store.put(newId, pair);
        primary.set(newId);
        log.warn(">>>> JWK rotated, new kid={}", newId);
    }

    public void removeOldKey() {
        for (String kid : store.keySet()) {
            if (!primary.get().equals(kid)) {
                store.remove(kid);
            }
        }
    }

    private KeyPair generateRsaKey() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /* 供 AuthorizationServer 使用：生成 JWKSet */
    public JWKSet jwkSet() {
        List<JWK> keys = store.entrySet().stream()
                .map(e -> {
                    RSAKey rsa = new RSAKey.Builder((RSAPublicKey) e.getValue().getPublic())
                            .privateKey((RSAPrivateKey) e.getValue().getPrivate())
                            .keyUse(KeyUse.SIGNATURE)
                            .algorithm(JWSAlgorithm.RS256)
                            .keyID(e.getKey())
                            .build();
                    return (JWK) rsa;
                }).toList();
        return new JWKSet(keys);
    }

    /* 根据 kid 取私钥，用于签发 Token */
    public RSAPrivateKey privateKey(String kid) {
        KeyPair kp = store.get(kid);
        return kp == null ? null : (RSAPrivateKey) kp.getPrivate();
    }

    /* 根据 kid 取公钥，用于验签 */
    public RSAPublicKey publicKey(String kid) {
        KeyPair kp = store.get(kid);
        return kp == null ? null : (RSAPublicKey) kp.getPublic();
    }

    public String primaryKid() {
        return primary.get();
    }


}
