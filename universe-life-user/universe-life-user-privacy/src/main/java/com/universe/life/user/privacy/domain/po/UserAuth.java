package com.universe.life.user.privacy.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户认证表
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_auth")
public class UserAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 认证类型：0 微信 1 qq 2 支付宝 3 微博 4 用户名 5 手机号 6 邮箱
     */
    private Integer identificationType;

    /**
     * 认证名
     */
    private String identification;

    /**
     * BCrypt 哈希后的密码
     */
    private String password;

    /**
     * 有效期
     */
    private Integer expiresIn;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 软删除时间戳（0 表示未删除）
     */
    private Long deleted;


}
