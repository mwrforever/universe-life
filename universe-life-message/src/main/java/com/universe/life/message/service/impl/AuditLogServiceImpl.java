package com.universe.life.message.service.impl;

import com.universe.life.message.domain.document.AuditLogDocument;
import com.universe.life.message.repository.AuditLogRepository;
import com.universe.life.message.service.AuditLogService;
import com.universe.life.message.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志服务实现
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    // 操作类型常量
    public static final String ACTION_MESSAGE_SEND = "MESSAGE_SEND";
    public static final String ACTION_MESSAGE_RECEIVE = "MESSAGE_RECEIVE";
    public static final String ACTION_MESSAGE_DELETE = "MESSAGE_DELETE";
    public static final String ACTION_MESSAGE_RECALL = "MESSAGE_RECALL";
    public static final String ACTION_FRIEND_REQUEST = "FRIEND_REQUEST";
    public static final String ACTION_FRIEND_ACCEPT = "FRIEND_ACCEPT";
    public static final String ACTION_FRIEND_REJECT = "FRIEND_REJECT";
    public static final String ACTION_FRIEND_DELETE = "FRIEND_DELETE";
    public static final String ACTION_GROUP_CREATE = "GROUP_CREATE";
    public static final String ACTION_GROUP_JOIN = "GROUP_JOIN";
    public static final String ACTION_GROUP_LEAVE = "GROUP_LEAVE";
    public static final String ACTION_GROUP_DISSOLVE = "GROUP_DISSOLVE";
    public static final String ACTION_ROOM_JOIN = "ROOM_JOIN";
    public static final String ACTION_ROOM_LEAVE = "ROOM_LEAVE";

    // 目标类型常量
    public static final String TARGET_MESSAGE = "MESSAGE";
    public static final String TARGET_FRIEND = "FRIEND";
    public static final String TARGET_GROUP = "GROUP";
    public static final String TARGET_ROOM = "ROOM";

    // 结果常量
    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAILED = "FAILED";

    @Override
    @Async
    public void logMessageSend(Long userId, String messageId, String messageType, String content) {
        String detail = String.format("{\"messageType\":\"%s\",\"contentLength\":%d}", 
                messageType, content != null ? content.length() : 0);
        log(userId, ACTION_MESSAGE_SEND, TARGET_MESSAGE, messageId, detail, RESULT_SUCCESS, null);
    }

    @Override
    @Async
    public void logMessageReceive(Long userId, String messageId, String messageType) {
        String detail = String.format("{\"messageType\":\"%s\"}", messageType);
        log(userId, ACTION_MESSAGE_RECEIVE, TARGET_MESSAGE, messageId, detail, RESULT_SUCCESS, null);
    }

    @Override
    @Async
    public void logMessageDelete(Long userId, String messageId) {
        log(userId, ACTION_MESSAGE_DELETE, TARGET_MESSAGE, messageId, null, RESULT_SUCCESS, null);
    }

    @Override
    @Async
    public void logMessageRecall(Long userId, String messageId) {
        log(userId, ACTION_MESSAGE_RECALL, TARGET_MESSAGE, messageId, null, RESULT_SUCCESS, null);
    }

    @Override
    @Async
    public void logFriendAction(Long userId, String action, Long friendId, String result) {
        log(userId, action, TARGET_FRIEND, String.valueOf(friendId), null, result, null);
    }

    @Override
    @Async
    public void logGroupAction(Long userId, String action, Long groupId, String detail, String result) {
        log(userId, action, TARGET_GROUP, String.valueOf(groupId), detail, result, null);
    }

    @Override
    @Async
    public void logRoomAction(Long userId, String action, Long roomId, String result) {
        log(userId, action, TARGET_ROOM, String.valueOf(roomId), null, result, null);
    }

    @Override
    @Async
    public void log(Long userId, String action, String targetType, String targetId,
                    String detail, String result, String errorMessage) {
        try {
            AuditLogDocument auditLog = new AuditLogDocument();
            auditLog.setId(snowflakeIdGenerator.nextIdStr());
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setDetail(detail);
            auditLog.setResult(result);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setCreatedAt(LocalDateTime.now());

            auditLogRepository.save(auditLog);
            log.debug("审计日志已记录: userId={}, action={}, targetType={}, targetId={}",
                    userId, action, targetType, targetId);
        } catch (Exception e) {
            log.error("记录审计日志失败: userId={}, action={}, error={}", userId, action, e.getMessage(), e);
        }
    }

    @Override
    public Page<AuditLogDocument> getUserLogs(Long userId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<AuditLogDocument> getActionLogs(String action, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }
}
