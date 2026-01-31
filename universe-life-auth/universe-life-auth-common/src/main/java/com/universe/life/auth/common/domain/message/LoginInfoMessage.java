package com.universe.life.auth.common.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录信息消息体
 * 用于通过 RabbitMQ 传递用户/员工登录信息
 *
 * @author universe-life
 * @since 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginInfoMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户/员工ID
     */
    private Long userId;

    /**
     * 登录IP地址
     */
    private String loginIp;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 登录类型: "user" 或 "employee"
     */
    private String loginType;
}
