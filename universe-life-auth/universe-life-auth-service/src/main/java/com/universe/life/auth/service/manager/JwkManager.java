package com.universe.life.auth.service.manager;

import cn.hutool.core.collection.CollUtil;
import com.nimbusds.jose.jwk.*;
import com.universe.life.auth.service.domain.po.Oauth2Jwk;
import com.universe.life.auth.service.enums.JwkAlgorithm;
import com.universe.life.auth.service.properties.JwkProperties;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static com.nimbusds.jose.jwk.Curve.P_256;

/**
 * JWK（JSON Web Key）管理器
 * <p>
 * 支持多种JWT签名算法的密钥生成、轮换和管理功能。
 * 当前支持的算法包括：
 * - RS256：RSA非对称算法，安全性高，适用于企业级应用
 * - ES256：ECDSA非对称算法，性能好，适用于移动端和IoT设备
 * - HS256：HMAC对称算法，性能最佳，适用于内部系统
 * </p>
 * <p>
 * 主要功能：
 * 1. 密钥生成：支持多种算法的密钥生成
 * 2. 密钥轮换：定期轮换密钥以提高安全性
 * 3. 密钥管理：存储和检索密钥对
 * 4. JWK集生成：生成符合JWK规范的密钥集
 * </p>
 *
 * @author 毛伟然
 * @since 2025/11/5 09:34
 */
@Slf4j
public class JwkManager {


    /**
     * 密钥存储容器
     * Key: 密钥ID（kid）
     * Value: 密钥对（KeyPair）或密钥（SecretKey）
     */
    private final ConcurrentHashMap<String, Object> store = new ConcurrentHashMap<>();

    /**
     * 算法类型存储容器
     * Key: 密钥ID（kid）
     * Value: 算法类型
     */
    private final ConcurrentHashMap<String, JwkAlgorithm> algorithmStore = new ConcurrentHashMap<>();

    /**
     * 主用密钥ID
     * 用于标识当前活跃的密钥
     */
    private final AtomicReferenceArray<String> primary;

    private final JwkProperties jwkProperties;


    public JwkManager(JwkProperties jwkProperties) {
        this.jwkProperties = jwkProperties;
        primary = new AtomicReferenceArray<>(jwkProperties.getPrimaryCount());
    }

    /**
     * 刷新所有密钥
     * <p>
     * 清空现有密钥并重新生成指定数量的主用密钥
     * </p>
     *
     */
    public void refresh() {
        store.clear();
        algorithmStore.clear();

        // 生成指定数量的主用密钥
        for (int i = 0; i < jwkProperties.getPrimaryCount(); i++) {
            String newId = UUID.randomUUID().toString().replace("-", "");
            Object key = generateKey(JwkAlgorithm.fromAlgorithm(jwkProperties.getDefaultAlgorithm()));

            // 存储密钥和算法类型
            store.put(newId, key);
            algorithmStore.put(newId, JwkAlgorithm.fromAlgorithm(jwkProperties.getDefaultAlgorithm()));
            primary.set(i, newId);

            log.info(">>>> 刷新生成密钥，算法类型: {}, 密钥ID: {}, 位置: {}",
                    jwkProperties.getDefaultAlgorithm(), newId, i);
        }

        log.info(">>>> 密钥刷新完成，共生成{}个主用密钥", jwkProperties.getPrimaryCount());
    }

    /**
     * 密钥轮换方法
     * <p>
     * 轮换流程：
     * 1. 根据配置的默认算法生成新密钥
     * 2. 将新密钥设置为主用密钥
     * 3. 旧密钥可保留一段时间，用于已发出Token的验签
     * 4. 记录轮换日志以便审计
     * </p>
     */
    public void rotate() {
        rotate(JwkAlgorithm.fromAlgorithm(jwkProperties.getDefaultAlgorithm()));
    }

    /**
     * 根据指定算法进行密钥轮换
     * <p>
     * 轮换所有主用密钥位置，生成新的密钥集替换所有现有的主用密钥
     * </p>
     *
     * @param algorithm 要使用的算法类型
     */
    public void rotate(JwkAlgorithm algorithm) {
        // 轮换所有主用密钥位置
        for (int i = 0; i < jwkProperties.getPrimaryCount(); i++) {
            String newId = UUID.randomUUID().toString().replace("-", "");
            Object key = generateKey(algorithm);

            // 存储密钥和算法类型
            store.put(newId, key);
            algorithmStore.put(newId, algorithm);
            primary.set(i, newId);

            log.info(">>>> JWK轮换完成，算法类型: {}, 密钥ID: {}, 位置: {}",
                    algorithm.getAlgorithm(), newId, i);
        }
        log.warn(">>>> 所有主用密钥轮换完成，共轮换{}个密钥", primary.length());
    }


    /**
     * 移除旧密钥
     * <p>
     * 清除非主用的旧密钥，释放内存空间。
     * 注意：在移除前应确保使用旧密钥签发的Token已过期。
     * </p>
     */
    public void removeOldKey() {
        if (primary == null) {
            log.warn(">>>> 主用密钥数组未初始化，无法清理旧密钥");
            return;
        }

        // 收集所有主用密钥ID
        java.util.Set<String> primaryKids = new java.util.HashSet<>();
        for (int i = 0; i < primary.length(); i++) {
            String kid = primary.get(i);
            if (kid != null) {
                primaryKids.add(kid);
            }
        }

        int removedCount = 0;

        for (String kid : store.keySet()) {
            if (!primaryKids.contains(kid)) {
                store.remove(kid);
                algorithmStore.remove(kid);
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info(">>>> 清理了{}个旧密钥，保留了{}个主用密钥", removedCount, primaryKids.size());
        }
    }

    /**
     * 根据算法类型生成密钥
     *
     * @param algorithm 算法类型
     * @return 生成的密钥对象（KeyPair或SecretKey）
     * @throws IllegalStateException 当密钥生成失败时抛出异常
     */
    private Object generateKey(JwkAlgorithm algorithm) {
        try {
            return switch (algorithm) {
                case RS256 -> generateRsaKey();
                case ES256 -> generateECKey();
                case HS256 -> generateHmacKey();
            };
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("密钥生成失败: " + algorithm.getAlgorithm(), e);
        }
    }

    /**
     * 生成RSA密钥对
     * <p>
     * 使用RSA-2048算法生成密钥对，适用于RS256签名算法
     * </p>
     *
     * @return RSA密钥对
     * @throws NoSuchAlgorithmException 当RSA算法不可用时抛出异常
     */
    private KeyPair generateRsaKey() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(JwkAlgorithm.RS256.getKeySize());
        return gen.generateKeyPair();
    }

    /**
     * 生成ECDSA密钥对
     * <p>
     * 使用P-256曲线生成EC密钥对，适用于ES256签名算法
     * </p>
     *
     * @return EC密钥对
     * @throws GeneralSecurityException 当EC算法不可用时抛出异常
     */
    private KeyPair generateECKey() throws GeneralSecurityException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec spec = new ECGenParameterSpec("secp256r1");
        gen.initialize(spec);
        return gen.generateKeyPair();
    }

    /**
     * 生成HMAC密钥
     * <p>
     * 生成256位的HMAC密钥，适用于HS256签名算法
     * </p>
     *
     * @return HMAC密钥
     * @throws NoSuchAlgorithmException 当HMAC算法不可用时抛出异常
     */
    private SecretKey generateHmacKey() throws NoSuchAlgorithmException {
        KeyGenerator gen = KeyGenerator.getInstance("HmacSHA256");
        gen.init(JwkAlgorithm.HS256.getKeySize());
        return gen.generateKey();
    }

    /**
     * 生成JWK集
     * <p>
     * 将所有存储的密钥转换为符合JWK规范的密钥集，
     * 用于AuthorizationServer的JWK Set端点发布公钥信息。
     * </p>
     *
     * @return JWK集，包含所有可用的公钥
     */
    public JWKSet jwkSet() {
        List<JWK> keys = store.entrySet().stream()
                .map(e -> createJWK(e.getKey(), e.getValue(), algorithmStore.get(e.getKey())))
                .toList();
        return new JWKSet(keys);
    }

    public JWKSet primaryJwkSet() {
        List<String> allPrimaryKids = allPrimaryKids();
        if (CollUtil.isEmpty(allPrimaryKids)) {
            return null;
        }
        List<JWK> keys = allPrimaryKids.stream()
                .map(kid -> createJWK(kid, store.get(kid), algorithmStore.get(kid)))
                .toList();
        return new JWKSet(keys);
    }

    /**
     * 根据密钥信息和算法类型创建JWK对象
     *
     * @param kid       密钥ID
     * @param key       密钥对象
     * @param algorithm 算法类型
     * @return JWK对象
     */
    private JWK createJWK(String kid, Object key, JwkAlgorithm algorithm) {
        try {
            return switch (algorithm) {
                case RS256 -> {
                    KeyPair rsaKeyPair = (KeyPair) key;
                    yield new RSAKey.Builder((RSAPublicKey) rsaKeyPair.getPublic())
                            .privateKey((RSAPrivateKey) rsaKeyPair.getPrivate())
                            .keyUse(KeyUse.SIGNATURE)
                            .algorithm(algorithm.getJwsAlgorithm())
                            .keyID(kid)
                            .build();
                }
                case ES256 -> {
                    KeyPair ecKeyPair = (KeyPair) key;
                    yield new ECKey.Builder(P_256, (ECPublicKey) ecKeyPair.getPublic())
                            .privateKey((ECPrivateKey) ecKeyPair.getPrivate())
                            .keyUse(KeyUse.SIGNATURE)
                            .algorithm(algorithm.getJwsAlgorithm())
                            .keyID(kid)
                            .build();
                }
                case HS256 -> {
                    SecretKey secretKey = (SecretKey) key;
                    yield new OctetSequenceKey.Builder(secretKey)
                            .keyUse(KeyUse.SIGNATURE)
                            .algorithm(algorithm.getJwsAlgorithm())
                            .keyID(kid)
                            .build();
                }
                default -> throw new IllegalArgumentException("不支持的算法类型: " + algorithm);
            };
        } catch (Exception e) {
            throw new IllegalStateException("JWK创建失败: " + algorithm.getAlgorithm(), e);
        }
    }

    /**
     * 根据密钥ID获取私钥
     * <p>
     * 用于JWT Token的签名操作。注意：对于HMAC算法，返回的是对称密钥。
     * </p>
     *
     * @param kid 密钥ID
     * @return 私钥或对称密钥，如果密钥不存在则返回null
     */
    public Object privateKey(String kid) {
        Object key = store.get(kid);
        if (key == null) {
            return null;
        }

        JwkAlgorithm algorithm = algorithmStore.get(kid);
        if (algorithm == null) {
            return null;
        }

        // 非对称算法返回私钥，对称算法返回密钥本身
        if (algorithm.isAsymmetric()) {
            KeyPair keyPair = (KeyPair) key;
            return switch (algorithm) {
                case RS256 -> (RSAPrivateKey) keyPair.getPrivate();
                case ES256 -> (ECPrivateKey) keyPair.getPrivate();
                default -> keyPair.getPrivate();
            };
        } else {
            return (SecretKey) key;
        }
    }

    /**
     * 根据密钥ID获取公钥
     * <p>
     * 用于JWT Token的验证操作。注意：对于HMAC算法，返回null，
     * 因为HMAC使用对称密钥，私钥和公钥是同一个。
     * </p>
     *
     * @param kid 密钥ID
     * @return 公钥，非对称算法返回公钥，对称算法返回null
     */
    public Object publicKey(String kid) {
        Object key = store.get(kid);
        if (key == null) {
            return null;
        }

        JwkAlgorithm algorithm = algorithmStore.get(kid);
        if (algorithm == null) {
            return null;
        }

        // 非对称算法返回公钥，对称算法返回null
        if (algorithm.isAsymmetric()) {
            KeyPair keyPair = (KeyPair) key;
            return switch (algorithm) {
                case RS256 -> (RSAPublicKey) keyPair.getPublic();
                case ES256 -> (ECPublicKey) keyPair.getPublic();
                default -> keyPair.getPublic();
            };
        } else {
            return null; // HMAC算法没有公钥
        }
    }

    /**
     * 获取主用密钥ID（第一个位置）
     *
     * @return 当前主用密钥的ID
     */
    public String primaryKid() {
        return primary != null ? primary.get(0) : null;
    }

    /**
     * 获取所有主用密钥ID
     *
     * @return 所有主用密钥ID的列表
     */
    public List<String> allPrimaryKids() {
        if (primary == null) {
            return new ArrayList<>();
        }

        List<String> kids = new ArrayList<>();
        for (int i = 0; i < primary.length(); i++) {
            String kid = primary.get(i);
            if (kid != null) {
                kids.add(kid);
            }
        }
        return kids;
    }

    /**
     * 获取主用密钥数量
     *
     * @return 主用密钥数组的大小
     */
    public int getPrimaryCount() {
        return primary != null ? primary.length() : 0;
    }

    /**
     * 获取主用密钥的算法类型（第一个位置）
     *
     * @return 主用密钥的算法类型，如果没有主用密钥则返回null
     */
    public JwkAlgorithm primaryAlgorithm() {
        String kid = primaryKid();
        return kid != null ? algorithmStore.get(kid) : null;
    }

    /**
     * 根据密钥ID获取算法类型
     *
     * @param kid 密钥ID
     * @return 算法类型，如果密钥不存在则返回null
     */
    public JwkAlgorithm getAlgorithm(String kid) {
        return algorithmStore.get(kid);
    }

    /**
     * 检查密钥是否存在
     *
     * @param kid 密钥ID
     * @return true表示密钥存在，false表示密钥不存在
     */
    public boolean containsKey(String kid) {
        return store.containsKey(kid);
    }

    /**
     * 获取存储的密钥数量
     *
     * @return 密钥数量
     */
    public int getKeyCount() {
        return store.size();
    }

    /**
     * 获取所有密钥ID
     *
     * @return 所有密钥ID的集合
     */
    public java.util.Set<String> getAllKeyIds() {
        return new HashSet<>(store.keySet());
    }

    /**
     * 手动添加密钥
     * <p>
     * 用于测试或特殊场景下的密钥管理
     * </p>
     *
     * @param kid       密钥ID
     * @param key       密钥对象
     * @param algorithm 算法类型
     * @param isPrimary 是否设置为主用密钥
     */
    public void addKey(String kid, Object key, JwkAlgorithm algorithm, boolean isPrimary) {
        store.put(kid, key);
        algorithmStore.put(kid, algorithm);

        if (isPrimary) {
            // 设置为第一个主用密钥位置
            if (primary != null && primary.length() > 0) {
                primary.set(0, kid);
            }
        }

        log.info(">>>> 添加密钥，算法类型: {}, 密钥ID: {}, 主用: {}",
                algorithm.getAlgorithm(), kid, isPrimary);
    }


    /**
     * 根据Oauth2Jwk实体列表转变并填充到本地缓存
     * <p>
     * 该方法从数据库加载JWK密钥信息，并将字符串形式的密钥数据转换为实际的密钥对象。
     * 支持RS256、ES256、HS256三种算法的密钥解析和缓存。
     * </p>
     *
     * @param oauth2Jwks JWK实体列表，从数据库查询得到
     * @return 转换后的JWKSet对象，用于AuthorizationServer的JWK Set端点
     * @throws IllegalArgumentException 当密钥数据格式错误或算法不支持时抛出异常
     */
    public JWKSet loadFromDatabase(List<Oauth2Jwk> oauth2Jwks) {
        if (oauth2Jwks == null || oauth2Jwks.isEmpty()) {
            log.warn(">>>> 数据库中没有JWK密钥数据，使用空的JWK集");
            return null;
        }

        log.info(">>>> 开始从数据库加载{}个JWK密钥", oauth2Jwks.size());

        // 清空现有缓存
        store.clear();
        algorithmStore.clear();
        if (primary != null) {
            for (int i = 0; i < primary.length(); i++) {
                primary.set(i, null);
            }
        }

        List<JWK> jwkList = new ArrayList<>();
        int primaryIndex = 0;

        for (Oauth2Jwk oauth2Jwk : oauth2Jwks) {
            try {
                // 根据数据库中的algorithm字段获取算法类型
                JwkAlgorithm algorithm = JwkAlgorithm.fromAlgorithm(oauth2Jwk.getAlgorithm());

                // 解析密钥字符串为实际的密钥对象
                Object key = parseKeyFromString(oauth2Jwk.getPublicKey(), oauth2Jwk.getPrivateKey(), algorithm);

                // 存储到缓存
                store.putIfAbsent(oauth2Jwk.getKid(), key);
                algorithmStore.putIfAbsent(oauth2Jwk.getKid(), algorithm);

                // 创建JWK对象并添加到列表
                JWK jwk = createJWK(oauth2Jwk.getKid(), key, algorithm);
                jwkList.add(jwk);

                // 设置主用密钥
                primary.set(primaryIndex, oauth2Jwk.getKid());
                primaryIndex++;

                log.debug(">>>> 成功加载JWK密钥，算法: {}, 密钥ID: {}, 状态: {}",
                        algorithm.getAlgorithm(), oauth2Jwk.getKid(), oauth2Jwk.getState());

            } catch (Exception e) {
                log.error(">>>> 加载JWK密钥失败，密钥ID: {}, 算法: {}, 错误: {}",
                        oauth2Jwk.getKid(), oauth2Jwk.getAlgorithm(), e.getMessage(), e);
                // 继续处理其他密钥，不中断整个加载过程
            }
        }

        // 如果没有找到主用密钥，使用第一个密钥作为主用密钥
        if (primaryIndex == 0 && primary != null && !oauth2Jwks.isEmpty()) {
            Oauth2Jwk firstJwk = oauth2Jwks.get(0);
            primary.set(0, firstJwk.getKid());
            log.warn(">>>> 未找到ACTIVE状态的密钥，使用第一个密钥作为主用密钥: {}", firstJwk.getKid());
        }

        String primaryKidInfo = primary != null ? primaryKid() : "null";
        log.info(">>>> JWK密钥加载完成，共加载{}个密钥，第一个主用密钥ID: {}", jwkList.size(), primaryKidInfo);

        return new JWKSet(jwkList);
    }


    /**
     * 将字符串形式的密钥数据解析为实际的密钥对象
     * <p>
     * 支持Base64编码的密钥字符串解析：
     * - RSA: 解析为KeyPair对象
     * - EC: 解析为KeyPair对象
     * - HMAC: 解析为SecretKey对象
     * </p>
     *
     * @param publicKeyStr  Base64编码的公钥字符串
     * @param privateKeyStr Base64编码的私钥字符串
     * @param algorithm     算法类型
     * @return 解析后的密钥对象（KeyPair或SecretKey）
     * @throws GeneralSecurityException 当密钥解析失败时抛出异常
     */
    private Object parseKeyFromString(String publicKeyStr, String privateKeyStr, JwkAlgorithm algorithm)
            throws GeneralSecurityException {
        try {
            return switch (algorithm) {
                case RS256 -> parseRSAKey(publicKeyStr, privateKeyStr);
                case ES256 -> parseECKey(publicKeyStr, privateKeyStr);
                case HS256 -> parseHMACKey(privateKeyStr); // HMAC算法只需要私钥（对称密钥）
            };
        } catch (Exception e) {
            throw new GeneralSecurityException("密钥解析失败: " + algorithm.getAlgorithm(), e);
        }
    }

    /**
     * 解析RSA密钥对
     *
     * @param publicKeyStr  Base64编码的RSA公钥
     * @param privateKeyStr Base64编码的RSA私钥
     * @return RSA密钥对
     * @throws GeneralSecurityException 当密钥格式错误时抛出异常
     */
    private KeyPair parseRSAKey(String publicKeyStr, String privateKeyStr) throws GeneralSecurityException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 解析公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

            // 解析私钥
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new GeneralSecurityException("RSA密钥解析失败", e);
        }
    }

    /**
     * 解析EC密钥对
     *
     * @param publicKeyStr  Base64编码的EC公钥
     * @param privateKeyStr Base64编码的EC私钥
     * @return EC密钥对
     * @throws GeneralSecurityException 当密钥格式错误时抛出异常
     */
    private KeyPair parseECKey(String publicKeyStr, String privateKeyStr) throws GeneralSecurityException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            // 解析公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);

            // 解析私钥
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new GeneralSecurityException("EC密钥解析失败", e);
        }
    }

    /**
     * 解析HMAC密钥
     *
     * @param keyStr Base64编码的HMAC密钥
     * @return HMAC密钥对象
     * @throws GeneralSecurityException 当密钥格式错误时抛出异常
     */
    private SecretKey parseHMACKey(String keyStr) throws GeneralSecurityException {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyStr);
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (Exception e) {
            throw new GeneralSecurityException("HMAC密钥解析失败", e);
        }
    }

}
