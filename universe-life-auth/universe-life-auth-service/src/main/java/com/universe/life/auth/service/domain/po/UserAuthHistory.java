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
 * 用户认证历史表：记录登录、登出等认证事件
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_auth_history")
public class UserAuthHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 认证历史记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 认证类型（LOGIN: 登录, LOGOUT: 登出, TOKEN_REFRESH: 令牌刷新）
     */
    private Integer authType;

    /**
     * 认证结果（SUCCESS: 成功, FAILURE: 失败）
     */
    private Integer authResult;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 用户代理字符串
     */
    private String userAgent;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 认证时间
     */
    private LocalDateTime authTime;

    /**
     * 失败原因（仅失败记录）
     */
    private String failureReason;

    /**
     * 地理位置（基于IP解析）
     */
    private String location;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 客户端ID（OAuth2认证时）
     */
    private String clientId;


}
