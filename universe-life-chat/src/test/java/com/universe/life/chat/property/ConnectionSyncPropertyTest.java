package com.universe.life.chat.property;

import com.universe.life.common.server.api.service.ChatServerLoadBalancer;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 连接数同步属性测试
 * 验证连接数同步一致性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class ConnectionSyncPropertyTest {

    @Autowired
    private ChatServerLoadBalancer loadBalancer;

    /**
     * Property 4: 连接数同步一致性
     * *For any* connection operations, the connection count should be consistent
     * 
     * **Validates: Requirements 3.5, 3.6**
     */
    @Property(tries = 50)
    @Label("Property 4: 连接数同步一致性")
    void connectionCountIsConsistent(
            @ForAll @IntRange(min = 1, max = 10) int connectionCount
    ) {
        String serverAddress = "test-sync-" + UUID.randomUUID().toString().substring(0, 8) + ":8080";

        // Given: 注册服务器
        loadBalancer.registerServer(serverAddress);

        try {
            // When: 增加连接数
            for (int i = 0; i < connectionCount; i++) {
                loadBalancer.incrementConnectionCount(serverAddress);
            }

            // Then: 连接数应该正确
            int actualCount = loadBalancer.getConnectionCount(serverAddress);
            assertEquals(connectionCount, actualCount, "连接数应该与增加次数一致");

            // When: 减少连接数
            for (int i = 0; i < connectionCount; i++) {
                loadBalancer.decrementConnectionCount(serverAddress);
            }

            // Then: 连接数应该为0
            int finalCount = loadBalancer.getConnectionCount(serverAddress);
            assertEquals(0, finalCount, "所有连接断开后连接数应该为0");
        } finally {
            // Cleanup
            loadBalancer.unregisterServer(serverAddress);
        }
    }

    /**
     * Property: 连接数增减操作原子性
     */
    @Property(tries = 50)
    @Label("连接数增减操作原子性")
    void connectionCountOperationsAreAtomic() {
        String serverAddress = "test-atomic-" + UUID.randomUUID().toString().substring(0, 8) + ":8080";
        loadBalancer.registerServer(serverAddress);

        try {
            // When: 执行增减操作
            int count1 = loadBalancer.incrementConnectionCount(serverAddress);
            int count2 = loadBalancer.incrementConnectionCount(serverAddress);
            int count3 = loadBalancer.decrementConnectionCount(serverAddress);

            // Then: 每次操作后的返回值应该正确
            assertEquals(1, count1, "第一次增加后应该是1");
            assertEquals(2, count2, "第二次增加后应该是2");
            assertEquals(1, count3, "减少后应该是1");

            // 最终值应该一致
            int finalCount = loadBalancer.getConnectionCount(serverAddress);
            assertEquals(1, finalCount, "最终连接数应该是1");
        } finally {
            loadBalancer.unregisterServer(serverAddress);
        }
    }

    /**
     * Property: 多服务器连接数独立
     */
    @Property(tries = 30)
    @Label("多服务器连接数独立")
    void multipleServersHaveIndependentCounts(
            @ForAll @IntRange(min = 1, max = 5) int server1Connections,
            @ForAll @IntRange(min = 1, max = 5) int server2Connections
    ) {
        String server1 = "test-multi-1-" + UUID.randomUUID().toString().substring(0, 8) + ":8080";
        String server2 = "test-multi-2-" + UUID.randomUUID().toString().substring(0, 8) + ":8080";

        loadBalancer.registerServer(server1);
        loadBalancer.registerServer(server2);

        try {
            // When: 分别增加连接数
            for (int i = 0; i < server1Connections; i++) {
                loadBalancer.incrementConnectionCount(server1);
            }
            for (int i = 0; i < server2Connections; i++) {
                loadBalancer.incrementConnectionCount(server2);
            }

            // Then: 各服务器连接数应该独立
            assertEquals(server1Connections, loadBalancer.getConnectionCount(server1),
                    "服务器1连接数应该正确");
            assertEquals(server2Connections, loadBalancer.getConnectionCount(server2),
                    "服务器2连接数应该正确");
        } finally {
            loadBalancer.unregisterServer(server1);
            loadBalancer.unregisterServer(server2);
        }
    }

    /**
     * Property: 连接数不会为负
     */
    @Property(tries = 50)
    @Label("连接数不会为负")
    void connectionCountNeverNegative(
            @ForAll @IntRange(min = 1, max = 10) int decrementCount
    ) {
        String serverAddress = "test-negative-" + UUID.randomUUID().toString().substring(0, 8) + ":8080";
        loadBalancer.registerServer(serverAddress);

        try {
            // When: 多次减少连接数（超过实际连接数）
            for (int i = 0; i < decrementCount; i++) {
                loadBalancer.decrementConnectionCount(serverAddress);
            }

            // Then: 连接数应该非负
            int count = loadBalancer.getConnectionCount(serverAddress);
            assertTrue(count >= 0, "连接数不应该为负: " + count);
        } finally {
            loadBalancer.unregisterServer(serverAddress);
        }
    }

    /**
     * Property: 服务器注销后连接数清零
     */
    @Property(tries = 30)
    @Label("服务器注销后连接数清零")
    void unregisterServerClearsConnectionCount(
            @ForAll @IntRange(min = 1, max = 10) int connectionCount
    ) {
        String serverAddress = "test-unregister-" + UUID.randomUUID().toString().substring(0, 8) + ":8080";
        loadBalancer.registerServer(serverAddress);

        // 增加一些连接
        for (int i = 0; i < connectionCount; i++) {
            loadBalancer.incrementConnectionCount(serverAddress);
        }

        // When: 注销服务器
        loadBalancer.unregisterServer(serverAddress);

        // Then: 服务器不再注册
        assertFalse(loadBalancer.isServerRegistered(serverAddress), "服务器应该已注销");

        // 连接数应该返回-1（表示服务器不存在）
        int count = loadBalancer.getConnectionCount(serverAddress);
        assertEquals(-1, count, "注销后连接数应该返回-1");
    }
}
