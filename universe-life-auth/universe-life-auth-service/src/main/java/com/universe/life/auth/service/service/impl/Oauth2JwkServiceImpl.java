package com.universe.life.auth.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.service.domain.po.Oauth2Jwk;
import com.universe.life.auth.service.enums.JwkState;
import com.universe.life.auth.service.manager.JwkManager;
import com.universe.life.auth.service.mapper.Oauth2JwkMapper;
import com.universe.life.auth.service.service.IOauth2JwkService;
import com.universe.life.common.constants.RabbitMqConstants;
import com.universe.life.common.domain.dto.UserAuthInfo;
import com.universe.life.common.exception.DatabaseException;
import com.universe.life.common.message.ExceptionMessage;
import com.universe.life.common.util.RabbitMqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    private final RabbitMqSender rabbitMqSender;


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
    @Transactional
    public Boolean update(String password) {
        // 对密码进行校验
        UserAuthInfo userAuthInfo = (UserAuthInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (ObjectUtil.isNull(userAuthInfo)) {
            throw new DatabaseException.QueryException(ExceptionMessage.USER_NOT_FOUND);
        }
        if (!bCryptPasswordEncoder.matches(password, userAuthInfo.getPassword())) {
            throw new DatabaseException.QueryException(ExceptionMessage.ACCOUNT_PASSWORD_ERROR);
        }
        // 先将数据库中的密钥失效
        // 实现将现有密钥状态设为INACTIVE的逻辑
        Set<String> allKeyIds = jwkManager.getAllKeyIds();
        if (CollUtil.isNotEmpty(allKeyIds)) {
            lambdaUpdate()
                    .set(Oauth2Jwk::getState, JwkState.INACTIVE.getState())
                    .in(Oauth2Jwk::getKid, allKeyIds)
                    .update();
        }
        // 轮换密钥
        jwkManager.refresh();
        // 将新密钥保存到数据库
        List<String> primaryKids = jwkManager.allPrimaryKids();
        List<Oauth2Jwk> newJwks = getOauth2Jwks(primaryKids);

        // 批量保存新密钥到数据库
        saveBatch(newJwks);
        // 通过rabbitmq通知其它服务立即将旧公钥加入黑名单
        rabbitMqSender.builder()
                .to(
                        RabbitMqConstants.Exchange.AUTH_NOTIFY_JWK_EXCHANGE,
                        RabbitMqConstants.Binding.AUTH_NOTIFY_BLACK_JWK_BINDING
                ).send(null);
        return true;
    }

    private List<Oauth2Jwk> getOauth2Jwks(List<String> primaryKids) {
        return primaryKids.stream().map(kid -> {
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
                .filter(Objects::nonNull) // 过滤掉失败的记录
                .toList();
    }

    @Override
    @Transactional
    public Boolean saveBatch(List<String> jwkids) {
        List<Oauth2Jwk> oauth2Jwks = getOauth2Jwks(jwkids);
        // 批量保存到数据库
        if (CollUtil.isEmpty(oauth2Jwks)) {
            return false;
        }
        return saveBatch(oauth2Jwks);
    }


}
