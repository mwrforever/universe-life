package com.universe.life.message.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 消息加密服务
 * 使用 AES 对称加密算法
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Component
public class MessageEncryptionService {

    private final AES aes;

    public MessageEncryptionService(
            @Value("${chat.encryption.secret-key:universe-life-chat-key}") String secretKey) {
        // 使用 MD5 将密钥转换为 16 字节
        byte[] key = SecureUtil.md5().digest(secretKey.getBytes(StandardCharsets.UTF_8));
        this.aes = SecureUtil.aes(key);
    }

    /**
     * 加密消息内容
     *
     * @param plainText 明文
     * @return 密文（Base64编码）
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        return aes.encryptBase64(plainText);
    }

    /**
     * 解密消息内容
     *
     * @param cipherText 密文（Base64编码）
     * @return 明文
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        return aes.decryptStr(cipherText);
    }
}
