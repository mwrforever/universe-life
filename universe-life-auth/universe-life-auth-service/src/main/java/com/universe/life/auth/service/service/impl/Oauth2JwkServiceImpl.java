package com.universe.life.auth.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nimbusds.jose.jwk.JWKSet;
import com.universe.life.auth.service.domain.po.Oauth2Jwk;
import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.auth.service.mapper.Oauth2JwkMapper;
import com.universe.life.auth.service.service.IOauth2JwkService;
import com.universe.life.common.domain.dto.UserAuthInfo;
import com.universe.life.common.exception.DatabaseException;
import com.universe.life.common.message.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * <p>
 * JWK表：存储JWK密钥对，用于签名和验证JWT令牌 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Oauth2JwkServiceImpl extends ServiceImpl<Oauth2JwkMapper, Oauth2Jwk> implements IOauth2JwkService {


    private final JwkManager jwkManager;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 从KeyPair中提取公钥和私钥的Base64编码字符串
     * <p>
     * 该方法用于将JWK密钥对转换为可存储到数据库的字符串格式。
     * 支持RSA和ECDSA非对称算法的密钥提取。
     * 注意：HS256算法不使用KeyPair，而是使用SecretKey。
     * </p>
     *
     * @param keyPair 密钥对对象
     * @param kid     密钥ID
     * @return 包含公钥和私钥Base64字符串的数组，索引0为公钥，索引1为私钥
     * @throws IllegalArgumentException 当密钥对为null或算法不支持时抛出异常
     */
    private String[] extractKeyPairFromKeyPair(KeyPair keyPair, String kid) {
        if (keyPair == null) {
            throw new IllegalArgumentException("密钥对不能为空");
        }

        if (kid == null || kid.trim().isEmpty()) {
            throw new IllegalArgumentException("密钥ID不能为空");
        }

        try {
            // 获取算法类型
            String algorithm = jwkManager.getAlgorithm(kid) != null ?
                    jwkManager.getAlgorithm(kid).getAlgorithm() : "RS256";

            return switch (algorithm) {
                case "RS256" -> extractRSAKeyPair(keyPair);
                case "ES256" -> extractECKeyPair(keyPair);
                case "HS256" -> {
                    // HS256算法不使用KeyPair，应该使用extractKeyPairFromJwkManager方法
                    throw new IllegalArgumentException("HS256算法应使用extractKeyPairFromJwkManager方法，不是KeyPair");
                }
                default -> throw new IllegalArgumentException("不支持的算法类型: " + algorithm);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("密钥对提取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取RSA密钥对的Base64编码字符串
     *
     * @param keyPair RSA密钥对
     * @return 包含公钥和私钥Base64字符串的数组
     */
    private String[] extractRSAKeyPair(KeyPair keyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        return new String[]{publicKeyStr, privateKeyStr};
    }

    /**
     * 提取ECDSA密钥对的Base64编码字符串
     *
     * @param keyPair ECDSA密钥对
     * @return 包含公钥和私钥Base64字符串的数组
     */
    private String[] extractECKeyPair(KeyPair keyPair) {
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        return new String[]{publicKeyStr, privateKeyStr};
    }

    /**
     * 从JwkManager中提取密钥对信息用于保存到数据库
     * <p>
     * 该方法从JwkManager的缓存中提取指定密钥ID的公钥和私钥，
     * 并转换为Base64编码的字符串格式，以便存储到数据库中。
     * 支持RS256、ES256和HS256算法的密钥提取。
     * </p>
     *
     * @param kid 密钥ID
     * @return 包含公钥和私钥Base64字符串的数组，索引0为公钥，索引1为私钥
     * @throws IllegalArgumentException 当密钥ID为空、密钥不存在或算法不支持时抛出异常
     */
    public String[] extractKeyPairFromJwkManager(String kid) {
        if (kid == null || kid.trim().isEmpty()) {
            throw new IllegalArgumentException("密钥ID不能为空");
        }

        // 检查密钥是否存在
        if (!jwkManager.containsKey(kid)) {
            throw new IllegalArgumentException("密钥ID不存在: " + kid);
        }

        try {
            // 获取算法类型
            String algorithm = jwkManager.getAlgorithm(kid) != null ?
                    jwkManager.getAlgorithm(kid).getAlgorithm() : "RS256";

            return switch (algorithm) {
                case "RS256" -> extractRSAKeysFromJwkManager(kid);
                case "ES256" -> extractECKeysFromJwkManager(kid);
                case "HS256" -> extractHSKeysFromJwkManager(kid);
                default -> throw new IllegalArgumentException("不支持的算法类型: " + algorithm);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("从JwkManager提取密钥对失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从JwkManager提取RSA密钥对
     *
     * @param kid 密钥ID
     * @return 包含公钥和私钥Base64字符串的数组
     */
    private String[] extractRSAKeysFromJwkManager(String kid) {
        // 从JwkManager获取公钥和私钥
        RSAPublicKey publicKey = (RSAPublicKey) jwkManager.publicKey(kid);
        RSAPrivateKey privateKey = (RSAPrivateKey) jwkManager.privateKey(kid);

        if (publicKey == null || privateKey == null) {
            throw new IllegalArgumentException("无法获取RSA密钥对: " + kid);
        }

        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        return new String[]{publicKeyStr, privateKeyStr};
    }

    /**
     * 从JwkManager提取ECDSA密钥对
     *
     * @param kid 密钥ID
     * @return 包含公钥和私钥Base64字符串的数组
     */
    private String[] extractECKeysFromJwkManager(String kid) {
        // 从JwkManager获取公钥和私钥
        ECPublicKey publicKey = (ECPublicKey) jwkManager.publicKey(kid);
        ECPrivateKey privateKey = (ECPrivateKey) jwkManager.privateKey(kid);

        if (publicKey == null || privateKey == null) {
            throw new IllegalArgumentException("无法获取ECDSA密钥对: " + kid);
        }

        String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        return new String[]{publicKeyStr, privateKeyStr};
    }

    /**
     * 从JwkManager提取HMAC对称密钥
     * <p>
     * HS256算法使用对称密钥，公钥和私钥是同一个密钥。
     * 因此，公钥和私钥字段都存储相同的密钥字符串。
     * </p>
     *
     * @param kid 密钥ID
     * @return 包含密钥Base64字符串的数组，索引0和索引1都是相同的密钥字符串
     */
    private String[] extractHSKeysFromJwkManager(String kid) {
        // 从JwkManager获取HMAC对称密钥
        SecretKey secretKey = (SecretKey) jwkManager.privateKey(kid);

        if (secretKey == null) {
            throw new IllegalArgumentException("无法获取HMAC密钥: " + kid);
        }

        // HMAC算法的公钥和私钥是同一个密钥
        String keyStr = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        // 对于对称算法，公钥和私钥都存储相同的密钥字符串
        return new String[]{keyStr, keyStr};
    }

    @Override
    public Boolean update(String password) {
        // 对密码进行校验
        UserAuthInfo userAuthInfo = (UserAuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (ObjectUtil.isNull(userAuthInfo)) {
            throw new DatabaseException.QueryException(ExceptionMessage.USER_NOT_FOUND);
        }
        if (!bCryptPasswordEncoder.matches(password, userAuthInfo.getPassword())) {
            throw new DatabaseException.QueryException(ExceptionMessage.ACCOUNT_PASSWORD_ERROR);
        }

        // TODO 通过rabbitmq通知其它服务立即将旧公钥加入黑名单

        // 先将数据库中的密钥失效
        // TODO: 实现将现有密钥状态设为INACTIVE的逻辑

        // 轮换密钥
        List<KeyPair> keyPairs = jwkManager.refresh();

        // 将新密钥保存到数据库
        List<String> primaryKids = jwkManager.allPrimaryKids();
        List<Oauth2Jwk> newJwks = primaryKids.stream().map(kid -> {
                    Oauth2Jwk oauth2Jwk = new Oauth2Jwk();
                    oauth2Jwk.setKid(kid);

                    // 使用密钥提取函数获取Base64编码的密钥字符串
                    try {
                        String[] keyPair = extractKeyPairFromJwkManager(kid);
                        oauth2Jwk.setPublicKey(keyPair[0]); // 公钥
                        oauth2Jwk.setPrivateKey(keyPair[1]); // 私钥
                    } catch (Exception e) {
                        // 记录错误但不中断处理
                        log.error("提取密钥对失败，密钥ID: {}, 错误: {}", kid, e.getMessage());
                        return null;
                    }

                    // 获取算法类型
                    String algorithm = jwkManager.getAlgorithm(kid) != null ?
                            jwkManager.getAlgorithm(kid).getAlgorithm() : "RS256";
                    oauth2Jwk.setAlgorithm(algorithm);
                    oauth2Jwk.setState("ACTIVE");
                    oauth2Jwk.setCreateTime(LocalDateTime.now());
                    oauth2Jwk.setExpireTime(LocalDateTime.now().plusDays(30));
                    return oauth2Jwk;
                })
                .filter(oauth2Jwk -> oauth2Jwk != null) // 过滤掉失败的记录
                .toList();

        // 批量保存新密钥到数据库
        boolean saveResult = saveBatch(newJwks);

        // TODO 通过rabbitmq更新其它服务的公钥并将清除黑名单

        return saveResult;
    }

    @Override
    @Transactional
    public Boolean saveBatch(JWKSet jwkSet) {
        List<Oauth2Jwk> oauth2Jwks = jwkSet.getKeys().stream().map(jwk -> {
            Oauth2Jwk oauth2Jwk = new Oauth2Jwk();
            oauth2Jwk.setKid(jwk.getKeyID());

            // 使用密钥提取函数从JwkManager获取Base64编码的密钥字符串
            try {
                String[] keyPair = extractKeyPairFromJwkManager(jwk.getKeyID());
                oauth2Jwk.setPublicKey(keyPair[0]); // 公钥
                oauth2Jwk.setPrivateKey(keyPair[1]); // 私钥
            } catch (Exception e) {
                // 如果提取失败，使用原来的JWK字符串作为备用方案
                oauth2Jwk.setPublicKey(jwk.toJSONString());
                oauth2Jwk.setPrivateKey(jwk.toJSONString());
            }
            oauth2Jwk.setAlgorithm(jwk.getAlgorithm().getName());
            oauth2Jwk.setState("ACTIVE");
            oauth2Jwk.setCreateTime(LocalDateTime.now());
            oauth2Jwk.setExpireTime(LocalDateTime.now().plusDays(30));
            return oauth2Jwk;
        }).toList();
        // 批量保存到数据库
        return saveBatch(oauth2Jwks);
    }


}
