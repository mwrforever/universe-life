package com.universe.life.message.service;

import com.universe.life.message.domain.document.AuditLogDocument;
import org.springframework.data.domain.Page;

/**
 * 审计日志服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface AuditLogService {

    /**
     * 记录消息发送日志
     */
    void logMessageSend(Long userId, String messageId, String messageType, String content);

    /**
     * 记录消息接收日志
     */
    void logMessageReceive(Long userId, String messageId, String messageType);

    /**
     * 记录消息删除日志
     */
    void logMessageDelete(Long userId, String messageId);

    /**
     * 记录消息撤回日志
     */
    void logMessageRecall(Long userId, String messageId);

    /**
     * 记录好友操作日志
     */
    void logFriendAction(Long userId, String action, Long friendId, String result);

    /**
     * 记录群组操作日志
     */
    void logGroupAction(Long userId, String action, Long groupId, String detail, String result);

    /**
     * 记录聊天室操作日志
     */
    void logRoomAction(Long userId, String action, Long roomId, String result);

    /**
     * 记录通用操作日志
     */
    void log(Long userId, String action, String targetType, String targetId, 
             String detail, String result, String errorMessage);

    /**
     * 查询用户审计日志
     */
    Page<AuditLogDocument> getUserLogs(Long userId, int pageNum, int pageSize);

    /**
     * 查询操作类型审计日志
     */
    Page<AuditLogDocument> getActionLogs(String action, int pageNum, int pageSize);
}
