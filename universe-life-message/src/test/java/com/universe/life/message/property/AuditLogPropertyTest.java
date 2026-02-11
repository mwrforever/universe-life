package com.universe.life.message.property;

import com.universe.life.message.domain.document.AuditLogDocument;
import com.universe.life.message.repository.AuditLogRepository;
import com.universe.life.message.service.AuditLogService;
import com.universe.life.message.service.impl.AuditLogServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 审计日志服务属性测试
 * 验证审计日志的完整性和可追溯性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class AuditLogPropertyTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Property 22: 审计日志完整性
     * For any logged action, the log should be retrievable with correct data
     * 
     * Validates: Requirements 14.3
     */
    @Property(tries = 100)
    @Label("Property 22: 审计日志完整性 - 消息发送日志")
    void auditLogIsComplete(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 1, max = 100) String content
    ) throws InterruptedException {
        Assume.that(content != null && !content.isBlank());

        String messageId = UUID.randomUUID().toString();
        String messageType = "TEXT";

        // 记录消息发送日志
        auditLogService.logMessageSend(userId, messageId, messageType, content);

        // 等待异步操作完成
        TimeUnit.MILLISECONDS.sleep(100);

        // 验证日志可查询
        Page<AuditLogDocument> logs = auditLogService.getUserLogs(userId, 1, 10);
        
        // 验证日志存在且数据正确
        boolean found = logs.getContent().stream()
                .anyMatch(log -> 
                        log.getUserId().equals(userId) &&
                        log.getTargetId().equals(messageId) &&
                        AuditLogServiceImpl.ACTION_MESSAGE_SEND.equals(log.getAction()) &&
                        AuditLogServiceImpl.RESULT_SUCCESS.equals(log.getResult())
                );
        
        assertTrue(found, "审计日志应包含消息发送记录");
    }

    /**
     * Property 23: 审计日志不可篡改性
     * For any audit log, once created, its core fields should remain unchanged
     * 
     * Validates: Requirements 14.3
     */
    @Property(tries = 50)
    @Label("Property 23: 审计日志不可篡改性")
    void auditLogIsImmutable(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long friendId
    ) throws InterruptedException {
        Assume.that(!userId.equals(friendId));

        // 记录好友操作日志
        auditLogService.logFriendAction(userId, AuditLogServiceImpl.ACTION_FRIEND_REQUEST, 
                friendId, AuditLogServiceImpl.RESULT_SUCCESS);

        TimeUnit.MILLISECONDS.sleep(100);

        // 第一次查询
        Page<AuditLogDocument> firstQuery = auditLogService.getUserLogs(userId, 1, 10);
        AuditLogDocument firstLog = firstQuery.getContent().stream()
                .filter(log -> log.getTargetId().equals(String.valueOf(friendId)))
                .findFirst()
                .orElse(null);

        assertNotNull(firstLog, "应能查询到审计日志");

        // 第二次查询，验证数据一致
        Page<AuditLogDocument> secondQuery = auditLogService.getUserLogs(userId, 1, 10);
        AuditLogDocument secondLog = secondQuery.getContent().stream()
                .filter(log -> log.getId().equals(firstLog.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(secondLog, "日志应持久存在");
        assertEquals(firstLog.getUserId(), secondLog.getUserId(), "用户ID不应改变");
        assertEquals(firstLog.getAction(), secondLog.getAction(), "操作类型不应改变");
        assertEquals(firstLog.getTargetId(), secondLog.getTargetId(), "目标ID不应改变");
        assertEquals(firstLog.getCreatedAt(), secondLog.getCreatedAt(), "创建时间不应改变");
    }

    /**
     * Property 24: 审计日志时序性
     * For any sequence of actions, logs should be ordered by creation time
     * 
     * Validates: Requirements 14.3
     */
    @Property(tries = 30)
    @Label("Property 24: 审计日志时序性")
    void auditLogsAreOrdered(
            @ForAll @LongRange(min = 1, max = 10000) Long userId
    ) throws InterruptedException {
        // 连续记录多条日志
        for (int i = 0; i < 3; i++) {
            String messageId = UUID.randomUUID().toString();
            auditLogService.logMessageSend(userId, messageId, "TEXT", "消息内容" + i);
            TimeUnit.MILLISECONDS.sleep(50);
        }

        TimeUnit.MILLISECONDS.sleep(100);

        // 查询日志
        Page<AuditLogDocument> logs = auditLogService.getUserLogs(userId, 1, 10);

        // 验证日志按时间倒序排列
        var logList = logs.getContent();
        for (int i = 0; i < logList.size() - 1; i++) {
            assertTrue(
                    !logList.get(i).getCreatedAt().isBefore(logList.get(i + 1).getCreatedAt()),
                    "日志应按创建时间倒序排列"
            );
        }
    }

    /**
     * Property 25: 审计日志按操作类型查询
     * For any action type, logs should be filterable by that type
     * 
     * Validates: Requirements 14.3
     */
    @Property(tries = 50)
    @Label("Property 25: 审计日志按操作类型查询")
    void auditLogsFilterableByAction(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long groupId
    ) throws InterruptedException {
        // 记录群组创建日志
        auditLogService.logGroupAction(userId, AuditLogServiceImpl.ACTION_GROUP_CREATE, 
                groupId, "{\"name\":\"测试群\"}", AuditLogServiceImpl.RESULT_SUCCESS);

        TimeUnit.MILLISECONDS.sleep(100);

        // 按操作类型查询
        Page<AuditLogDocument> logs = auditLogService.getActionLogs(
                AuditLogServiceImpl.ACTION_GROUP_CREATE, 1, 100);

        // 验证所有返回的日志都是指定操作类型
        logs.getContent().forEach(log -> 
                assertEquals(AuditLogServiceImpl.ACTION_GROUP_CREATE, log.getAction(),
                        "查询结果应只包含指定操作类型的日志")
        );
    }

    /**
     * Property 26: 审计日志失败记录
     * For any failed action, the log should capture error information
     * 
     * Validates: Requirements 14.3
     */
    @Property(tries = 50)
    @Label("Property 26: 审计日志失败记录")
    void auditLogCapturesFailures(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 1, max = 50) String errorMessage
    ) throws InterruptedException {
        Assume.that(errorMessage != null && !errorMessage.isBlank());

        String targetId = UUID.randomUUID().toString();

        // 记录失败操作日志
        auditLogService.log(userId, AuditLogServiceImpl.ACTION_MESSAGE_SEND, 
                AuditLogServiceImpl.TARGET_MESSAGE, targetId, null, 
                AuditLogServiceImpl.RESULT_FAILED, errorMessage);

        TimeUnit.MILLISECONDS.sleep(100);

        // 查询日志
        Page<AuditLogDocument> logs = auditLogService.getUserLogs(userId, 1, 10);

        // 验证失败日志包含错误信息
        boolean found = logs.getContent().stream()
                .anyMatch(log -> 
                        log.getTargetId().equals(targetId) &&
                        AuditLogServiceImpl.RESULT_FAILED.equals(log.getResult()) &&
                        errorMessage.equals(log.getErrorMessage())
                );

        assertTrue(found, "失败操作的审计日志应包含错误信息");
    }

    @Provide
    Arbitrary<String> resultStatus() {
        return Arbitraries.of(
                AuditLogServiceImpl.RESULT_SUCCESS,
                AuditLogServiceImpl.RESULT_FAILED
        );
    }
}
