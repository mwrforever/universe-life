package com.universe.life.message.domain.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 审计日志 MongoDB 文档
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Document(collection = "audit_log")
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_time", def = "{'userId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_action_time", def = "{'action': 1, 'createdAt': -1}")
})
public class AuditLogDocument {

    /**
     * 日志ID
     */
    @Id
    private String id;

    /**
     * 用户ID
     */
    @Indexed
    private Long userId;

    /**
     * 操作类型
     */
    @Indexed
    private String action;

    /**
     * 目标类型（MESSAGE, GROUP, FRIEND, ROOM）
     */
    private String targetType;

    /**
     * 目标ID
     */
    private String targetId;

    /**
     * 操作详情（JSON格式）
     */
    private String detail;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 操作结果（SUCCESS, FAILED）
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @Indexed
    private LocalDateTime createdAt;
}
