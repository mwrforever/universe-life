package com.universe.life.auth.service.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.Getter;

/**
 * JWK（JSON Web Key）算法枚举
 * <p>
 * 定义了支持的JWT签名算法类型，包含RSA、ECDSA、HMAC三大类算法
 * 每种算法都有其特定的用途和安全性特点
 * </p>
 *
 * @author 毛伟然
 * @since 2025/11/10
 */
@Getter
public enum JwkAlgorithm {

    /**
     * RSASSA-PKCS1-v1_5 算法，使用RSA-2048密钥
     * <p>
     * 特点：
     * - 非对称算法，公钥验签，私钥签名
     * - 兼容性好，广泛支持
     * - 密钥长度2048位，安全性较高
     * - 性能相对较慢，但安全性强
     * </p>
     * 适用场景：需要高安全性的企业级应用，对外暴露的API
     */
    RS256("RS256", JWSAlgorithm.RS256, "RSA", 2048, "RSASSA-PKCS1-v1_5 using SHA-256"),

    /**
     * ECDSA 算法，使用P-256曲线
     * <p>
     * 特点：
     * - 非对称算法，公钥验签，私钥签名
     * - 密钥更小，签名更短，传输效率高
     * - 性能优于RSA，安全性相当
     * - 现代化算法，适合移动端和IoT设备
     * </p>
     * 适用场景：移动应用、IoT设备、对性能要求较高的场景
     */
    ES256("ES256", JWSAlgorithm.ES256, "EC", 256, "ECDSA using P-256 curve and SHA-256"),

    /**
     * HMAC 算法，使用SHA-256哈希
     * <p>
     * 特点：
     * - 对称算法，同一个密钥用于签名和验签
     * - 性能最快，实现简单
     * - 密钥管理要求高，需要安全地共享密钥
     * - 适用于内部系统或可信环境
     * </p>
     * 适用场景：内部服务间通信、单体应用、高并发场景
     */
    HS256("HS256", JWSAlgorithm.HS256, "HMAC", 256, "HMAC using SHA-256");

    /**
     * 算法标识符
     */
    @EnumValue
    @JsonValue
    private final String algorithm;

    /**
     * JWS算法对象
     */
    private final JWSAlgorithm jwsAlgorithm;

    /**
     * 密钥类型：RSA、EC、HMAC
     */
    private final String keyType;

    /**
     * 密钥长度（位）
     */
    private final int keySize;

    /**
     * 算法描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param algorithm    算法标识符
     * @param jwsAlgorithm JWS算法对象
     * @param keyType      密钥类型
     * @param keySize      密钥长度
     * @param description  算法描述
     */
    JwkAlgorithm(String algorithm, JWSAlgorithm jwsAlgorithm, String keyType, int keySize, String description) {
        this.algorithm = algorithm;
        this.jwsAlgorithm = jwsAlgorithm;
        this.keyType = keyType;
        this.keySize = keySize;
        this.description = description;
    }

    /**
     * 根据算法标识符查找枚举
     *
     * @param algorithm 算法标识符（如"RS256"、"ES256"、"HS256"）
     * @return 对应的算法枚举
     * @throws IllegalArgumentException 当算法不支持时抛出异常
     */
    public static JwkAlgorithm fromAlgorithm(String algorithm) {
        for (JwkAlgorithm jwkAlgorithm : values()) {
            if (jwkAlgorithm.algorithm.equals(algorithm)) {
                return jwkAlgorithm;
            }
        }
        throw new IllegalArgumentException("不支持的JWK算法: " + algorithm + "，支持的算法有: " + getSupportedAlgorithms());
    }

    /**
     * 检查是否为非对称算法
     *
     * @return true表示非对称算法（RSA、EC），false表示对称算法（HMAC）
     */
    public boolean isAsymmetric() {
        return !"HMAC".equals(keyType);
    }

    /**
     * 检查是否为RSA算法
     *
     * @return true表示RSA算法
     */
    public boolean isRSA() {
        return "RSA".equals(keyType);
    }

    /**
     * 检查是否为ECDSA算法
     *
     * @return true表示ECDSA算法
     */
    public boolean isEC() {
        return "EC".equals(keyType);
    }

    /**
     * 检查是否为HMAC算法
     *
     * @return true表示HMAC算法
     */
    public boolean isHMAC() {
        return "HMAC".equals(keyType);
    }

    /**
     * 获取所有支持的算法标识符
     *
     * @return 支持的算法列表
     */
    public static String[] getSupportedAlgorithms() {
        JwkAlgorithm[] algorithms = values();
        String[] result = new String[algorithms.length];
        for (int i = 0; i < algorithms.length; i++) {
            result[i] = algorithms[i].algorithm;
        }
        return result;
    }

    /**
     * 获取算法的详细描述信息
     *
     * @return 包含算法类型、密钥长度、用途等信息的描述
     */
    public String getDetailedDescription() {
        return String.format("%s (%s) - %s - 密钥长度: %d位",
                algorithm, keyType, description, keySize);
    }

    @Override
    public String toString() {
        return String.format("%s - %s", algorithm, description);
    }
}