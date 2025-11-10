package com.universe.life.auth.service.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * JWK表：存储JWK密钥对，用于签名和验证JWT令牌
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("oauth2_jwk")
public class Oauth2Jwk implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("kid")
    private String kid;

    @TableField("public_key")
    private String publicKey;

    @TableField("private_key")
    private String privateKey;

    @TableField("algorithm")
    private String algorithm;

    @TableField("state")
    private String state;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;


}
