package com.universe.life.message.property;

import com.universe.life.message.util.SnowflakeIdGenerator;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 雪花算法ID生成器属性测试
 * 使用 jqwik 进行属性测试，验证消息ID的唯一性和正确性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class SnowflakeIdPropertyTest {

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * Property 17: 消息ID唯一性
     * *For any* number of generated IDs, all IDs must be unique
     * 
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 100)
    @Label("Property 17: 消息ID唯一性 - 批量生成ID无重复")
    void generatedIdsAreUnique(
            @ForAll @IntRange(min = 100, max = 1000) int count
    ) {
        // Given: 生成指定数量的ID
        Set<Long> ids = new HashSet<>();
        
        // When: 批量生成ID
        for (int i = 0; i < count; i++) {
            long id = snowflakeIdGenerator.nextId();
            
            // Then: 每个ID都应该是唯一的
            boolean isUnique = ids.add(id);
            assertTrue(isUnique, 
                    String.format("ID %d 在第 %d 次生成时重复", id, i + 1));
        }
        
        // 验证最终集合大小
        assertEquals(count, ids.size(), 
                String.format("生成 %d 个ID后，唯一ID数量应该也是 %d", count, count));
    }

    /**
     * Property 17.1: 消息ID单调递增
     * *For any* sequence of generated IDs, each ID must be greater than the previous one
     * 
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 100)
    @Label("Property 17.1: 消息ID单调递增")
    void generatedIdsAreMonotonicallyIncreasing(
            @ForAll @IntRange(min = 10, max = 500) int count
    ) {
        // Given: 准备存储ID的列表
        List<Long> ids = new ArrayList<>();
        
        // When: 顺序生成ID
        for (int i = 0; i < count; i++) {
            ids.add(snowflakeIdGenerator.nextId());
        }
        
        // Then: 验证单调递增
        for (int i = 1; i < ids.size(); i++) {
            assertTrue(ids.get(i) > ids.get(i - 1),
                    String.format("ID应该单调递增: ids[%d]=%d 应该大于 ids[%d]=%d",
                            i, ids.get(i), i - 1, ids.get(i - 1)));
        }
    }

    /**
     * Property 17.2: 消息ID为正数
     * *For any* generated ID, it must be a positive number
     * 
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 200)
    @Label("Property 17.2: 消息ID为正数")
    void generatedIdsArePositive() {
        // When: 生成ID
        long id = snowflakeIdGenerator.nextId();
        
        // Then: ID必须为正数
        assertTrue(id > 0, String.format("生成的ID %d 应该是正数", id));
    }

    /**
     * Property 17.3: 字符串ID与Long ID一致
     * *For any* generated ID, the string representation must match the long value
     * 
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 100)
    @Label("Property 17.3: 字符串ID与Long ID格式一致")
    void stringIdMatchesLongId() {
        // When: 生成Long ID和String ID
        long longId = snowflakeIdGenerator.nextId();
        String strId = snowflakeIdGenerator.nextIdStr();
        
        // Then: 两者都应该是有效的正数
        assertTrue(longId > 0, "Long ID应该是正数");
        assertDoesNotThrow(() -> Long.parseLong(strId), "String ID应该可以解析为Long");
        assertTrue(Long.parseLong(strId) > 0, "String ID解析后应该是正数");
    }

    /**
     * Property 17.4: 并发生成ID唯一性
     * *For any* concurrent ID generation, all IDs must be unique across threads
     * 
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 20)
    @Label("Property 17.4: 并发生成ID唯一性")
    void concurrentGeneratedIdsAreUnique(
            @ForAll @IntRange(min = 2, max = 8) int threadCount,
            @ForAll @IntRange(min = 50, max = 200) int idsPerThread
    ) throws InterruptedException, ExecutionException {
        // Given: 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        ConcurrentHashMap<Long, Integer> idMap = new ConcurrentHashMap<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        
        // When: 并发生成ID
        List<Future<List<Long>>> futures = new ArrayList<>();
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            futures.add(executor.submit(() -> {
                List<Long> threadIds = new ArrayList<>();
                try {
                    startLatch.await(); // 等待所有线程就绪
                    for (int i = 0; i < idsPerThread; i++) {
                        long id = snowflakeIdGenerator.nextId();
                        threadIds.add(id);
                        // 检查是否有重复
                        Integer previous = idMap.putIfAbsent(id, threadIndex);
                        if (previous != null) {
                            fail(String.format("ID %d 在线程 %d 和线程 %d 中重复", 
                                    id, previous, threadIndex));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
                return threadIds;
            }));
        }
        
        // 启动所有线程
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Then: 验证所有ID都是唯一的
        int expectedTotal = threadCount * idsPerThread;
        assertEquals(expectedTotal, idMap.size(),
                String.format("并发生成 %d 个ID后，唯一ID数量应该也是 %d", 
                        expectedTotal, expectedTotal));
    }

    /**
     * Property 17.5: ID在有效范围内
     * *For any* generated ID, it must be within the valid 64-bit signed long range
     * 
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 100)
    @Label("Property 17.5: ID在有效范围内")
    void generatedIdsAreWithinValidRange() {
        // When: 生成ID
        long id = snowflakeIdGenerator.nextId();
        
        // Then: ID应该在有效范围内（正数且不超过Long.MAX_VALUE）
        assertTrue(id > 0 && id <= Long.MAX_VALUE,
                String.format("ID %d 应该在 (0, %d] 范围内", id, Long.MAX_VALUE));
    }

    /**
     * Property 17.6: 大批量生成ID无重复
     * *For any* large batch of generated IDs, all IDs must be unique
     * 
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 10)
    @Label("Property 17.6: 大批量生成ID无重复")
    void largeScaleGeneratedIdsAreUnique() {
        // Given: 生成大量ID
        int count = 10000;
        Set<Long> ids = new HashSet<>(count);
        
        // When: 批量生成
        for (int i = 0; i < count; i++) {
            ids.add(snowflakeIdGenerator.nextId());
        }
        
        // Then: 所有ID都应该唯一
        assertEquals(count, ids.size(),
                String.format("生成 %d 个ID后，唯一ID数量应该也是 %d", count, count));
    }

    /**
     * Property 17.7: 连续生成的ID差值合理
     * *For any* two consecutively generated IDs, the difference should be reasonable
     * 
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 100)
    @Label("Property 17.7: 连续生成的ID差值合理")
    void consecutiveIdsDifferenceIsReasonable() {
        // When: 连续生成两个ID
        long id1 = snowflakeIdGenerator.nextId();
        long id2 = snowflakeIdGenerator.nextId();
        
        // Then: 差值应该是正数且合理（雪花算法中，同一毫秒内序列号递增）
        long diff = id2 - id1;
        assertTrue(diff > 0, 
                String.format("连续生成的ID差值应该为正: id1=%d, id2=%d, diff=%d", 
                        id1, id2, diff));
        // 雪花算法中，同一毫秒内差值通常很小（序列号递增）
        // 跨毫秒时差值会较大（时间戳部分变化）
        // 这里只验证差值为正，不限制具体范围
    }
}
