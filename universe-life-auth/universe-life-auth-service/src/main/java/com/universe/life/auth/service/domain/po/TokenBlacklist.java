package com.universe.life.auth.service.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 令牌黑名单表：存储被撤销的JWT令牌
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("token_blacklist")
public class TokenBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 黑名单记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * JWT令牌ID（jti声明）
     */
    private String tokenId;

    /**
     * 令牌类型（ACCESS: 访问令牌, REFRESH: 刷新令牌）
     */
    private Integer tokenType;

    /**
     * 关联用户名
     */
    private String username;

    /**
     * 关联客户端ID
     */
    private String clientId;

    /**
     * 撤销时间
     */
    private LocalDateTime revokedTime;

    /**
     * 撤销原因
     */
    private String revokeReason;

    /**
     * 令牌原始过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 撤销操作者
     */
    private String revokedBy;


}
