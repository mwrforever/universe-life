package com.universe.life.message.property;

import com.universe.life.message.service.IdempotentHandler;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息幂等处理器属性测试
 * 验证消息幂等性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class IdempotentHandlerPropertyTest {

    @Autowired
    private IdempotentHandler idempotentHandler;

    /**
     * Property 19: 消息幂等性
     * *For any* message ID, tryProcess should return true only once
     * 
     * **Validates: Requirements 11.1**
     */
    @Property(tries = 100)
    @Label("Property 19: 消息幂等性 - tryProcess 只成功一次")
    void tryProcessSucceedsOnlyOnce() {
        // Given: 生成唯一的消息ID
        String messageId = UUID.randomUUID().toString();

        // When: 第一次尝试处理
        boolean firstAttempt = idempotentHandler.tryProcess(messageId);

        // Then: 第一次应该成功
        assertTrue(firstAttempt, "第一次处理应该成功");

        // When: 第二次尝试处理
        boolean secondAttempt = idempotentHandler.tryProcess(messageId);

        // Then: 第二次应该失败
        assertFalse(secondAttempt, "第二次处理应该失败（幂等）");

        // Cleanup
        idempotentHandler.removeProcessed(messageId);
    }

    /**
     * Property: isProcessed 状态一致性
     * *For any* processed message, isProcessed should return true
     * 
     * **Validates: Requirements 11.1**
     */
    @Property(tries = 100)
    @Label("isProcessed 状态一致性")
    void isProcessedReflectsState() {
        // Given: 生成唯一的消息ID
        String messageId = UUID.randomUUID().toString();

        // When: 检查未处理的消息
        boolean beforeProcess = idempotentHandler.isProcessed(messageId);

        // Then: 应该返回 false
        assertFalse(beforeProcess, "未处理的消息应该返回 false");

        // When: 处理消息
        idempotentHandler.tryProcess(messageId);

        // Then: 应该返回 true
        boolean afterProcess = idempotentHandler.isProcessed(messageId);
        assertTrue(afterProcess, "已处理的消息应该返回 true");

        // Cleanup
        idempotentHandler.removeProcessed(messageId);
    }

    /**
     * Property: markProcessed 标记消息为已处理
     * *For any* message, markProcessed should mark it as processed
     * 
     * **Validates: Requirements 11.1**
     */
    @Property(tries = 100)
    @Label("markProcessed 标记消息为已处理")
    void markProcessedMarksMessage() {
        // Given: 生成唯一的消息ID
        String messageId = UUID.randomUUID().toString();

        // When: 标记消息为已处理
        idempotentHandler.markProcessed(messageId);

        // Then: isProcessed 应该返回 true
        assertTrue(idempotentHandler.isProcessed(messageId), "标记后应该返回 true");

        // And: tryProcess 应该返回 false
        assertFalse(idempotentHandler.tryProcess(messageId), "标记后 tryProcess 应该返回 false");

        // Cleanup
        idempotentHandler.removeProcessed(messageId);
    }

    /**
     * Property: removeProcessed 移除处理标记
     * *For any* processed message, removeProcessed should allow reprocessing
     * 
     * **Validates: Requirements 11.2**
     */
    @Property(tries = 100)
    @Label("removeProcessed 移除处理标记")
    void removeProcessedAllowsReprocessing() {
        // Given: 处理一条消息
        String messageId = UUID.randomUUID().toString();
        idempotentHandler.tryProcess(messageId);
        assertTrue(idempotentHandler.isProcessed(messageId), "消息应该已处理");

        // When: 移除处理标记
        idempotentHandler.removeProcessed(messageId);

        // Then: 消息应该可以重新处理
        assertFalse(idempotentHandler.isProcessed(messageId), "移除后应该返回 false");
        assertTrue(idempotentHandler.tryProcess(messageId), "移除后应该可以重新处理");

        // Cleanup
        idempotentHandler.removeProcessed(messageId);
    }

    /**
     * Property: 并发幂等性
     * *For any* message ID, concurrent tryProcess calls should only succeed once
     * 
     * **Validates: Requirements 11.1**
     */
    @Property(tries = 50)
    @Label("并发幂等性")
    void concurrentTryProcessSucceedsOnlyOnce(
            @ForAll @IntRange(min = 2, max = 10) int threadCount
    ) throws InterruptedException {
        // Given: 生成唯一的消息ID
        String messageId = UUID.randomUUID().toString();
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // When: 多线程并发尝试处理
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程就绪
                    if (idempotentHandler.tryProcess(messageId)) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 同时启动所有线程
        endLatch.await();
        executor.shutdown();

        // Then: 只有一个线程应该成功
        assertEquals(1, successCount.get(), "并发处理时只有一个线程应该成功");

        // Cleanup
        idempotentHandler.removeProcessed(messageId);
    }

    /**
     * Property: 不同消息ID独立处理
     * *For any* two different message IDs, they should be processed independently
     * 
     * **Validates: Requirements 11.1**
     */
    @Property(tries = 100)
    @Label("不同消息ID独立处理")
    void differentMessageIdsAreIndependent() {
        // Given: 生成两个不同的消息ID
        String messageId1 = UUID.randomUUID().toString();
        String messageId2 = UUID.randomUUID().toString();

        // When: 处理第一个消息
        boolean result1 = idempotentHandler.tryProcess(messageId1);

        // Then: 第一个消息处理成功
        assertTrue(result1, "第一个消息应该处理成功");

        // When: 处理第二个消息
        boolean result2 = idempotentHandler.tryProcess(messageId2);

        // Then: 第二个消息也应该处理成功（独立）
        assertTrue(result2, "第二个消息应该独立处理成功");

        // Verify: 两个消息都被标记为已处理
        assertTrue(idempotentHandler.isProcessed(messageId1), "第一个消息应该已处理");
        assertTrue(idempotentHandler.isProcessed(messageId2), "第二个消息应该已处理");

        // Cleanup
        idempotentHandler.removeProcessed(messageId1);
        idempotentHandler.removeProcessed(messageId2);
    }

    /**
     * Property: 空消息ID处理
     * *For any* null or empty message ID, tryProcess should return true (allow processing)
     */
    @Property(tries = 10)
    @Label("空消息ID处理")
    void nullOrEmptyMessageIdAllowsProcessing() {
        // When & Then: null 消息ID
        assertTrue(idempotentHandler.tryProcess(null), "null 消息ID应该允许处理");
        assertFalse(idempotentHandler.isProcessed(null), "null 消息ID不应该被标记");

        // When & Then: 空字符串消息ID
        assertTrue(idempotentHandler.tryProcess(""), "空字符串消息ID应该允许处理");
        assertFalse(idempotentHandler.isProcessed(""), "空字符串消息ID不应该被标记");
    }
}
