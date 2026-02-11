package com.universe.life.message.property;

import com.universe.life.message.domain.po.PublicRoom;
import com.universe.life.message.enums.RoomStatus;
import com.universe.life.message.mapper.PublicRoomMapper;
import com.universe.life.message.service.PublicRoomService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 公共聊天室属性测试
 * 验证公共聊天室订阅一致性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class PublicRoomPropertyTest {

    @Autowired
    private PublicRoomService publicRoomService;

    @Autowired
    private PublicRoomMapper publicRoomMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String ROOM_ONLINE_KEY_PREFIX = "chat:room:online:";

    /**
     * Property 15: 公共聊天室订阅一致性
     * *For any* room, join and leave operations should be consistent
     * 
     * **Validates: Requirements 8.2, 8.3**
     */
    @Property(tries = 50)
    @Label("Property 15: 公共聊天室订阅一致性")
    void roomSubscriptionIsConsistent(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 1, max = 50) String roomName
    ) {
        Assume.that(roomName != null && !roomName.isBlank());

        // Given: 创建测试聊天室
        PublicRoom room = PublicRoom.builder()
                .name(roomName + "_" + System.currentTimeMillis())
                .description("Test room")
                .maxOnline(100)
                .currentOnline(0)
                .totalMembers(0)
                .status(RoomStatus.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        publicRoomMapper.insert(room);
        Long roomId = room.getId();

        try {
            // When: 用户加入聊天室
            publicRoomService.joinRoom(roomId, userId);

            // Then: 在线人数应该增加
            PublicRoom afterJoin = publicRoomMapper.selectById(roomId);
            assertTrue(afterJoin.getCurrentOnline() >= 1, "加入后在线人数应该至少为1");

            // When: 用户离开聊天室
            publicRoomService.leaveRoom(roomId, userId);

            // Then: 在线人数应该减少
            PublicRoom afterLeave = publicRoomMapper.selectById(roomId);
            assertTrue(afterLeave.getCurrentOnline() < afterJoin.getCurrentOnline() || afterLeave.getCurrentOnline() == 0,
                    "离开后在线人数应该减少");
        } finally {
            // Cleanup
            publicRoomMapper.deleteById(roomId);
            redisTemplate.delete(ROOM_ONLINE_KEY_PREFIX + roomId);
        }
    }

    /**
     * Property: 在线人数非负
     * 验证即使多次离开操作，在线人数也不会变成负数
     */
    @Property(tries = 50)
    @Label("在线人数非负")
    void onlineCountIsNonNegative(
            @ForAll @LongRange(min = 1, max = 10000) Long userId
    ) {
        // Given: 创建测试聊天室
        PublicRoom room = PublicRoom.builder()
                .name("NonNegativeTest_" + System.currentTimeMillis())
                .description("Test room")
                .maxOnline(100)
                .currentOnline(0)
                .totalMembers(0)
                .status(RoomStatus.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        publicRoomMapper.insert(room);
        Long roomId = room.getId();

        try {
            // When: 多次离开（即使没有加入）
            for (int i = 0; i < 5; i++) {
                try {
                    publicRoomService.leaveRoom(roomId, userId + i);
                } catch (Exception ignored) {
                    // 忽略异常
                }
            }

            // Then: 在线人数应该非负
            PublicRoom afterLeave = publicRoomMapper.selectById(roomId);
            assertTrue(afterLeave.getCurrentOnline() >= 0, 
                    "在线人数不应该为负: " + afterLeave.getCurrentOnline());
        } finally {
            // Cleanup
            publicRoomMapper.deleteById(roomId);
            redisTemplate.delete(ROOM_ONLINE_KEY_PREFIX + roomId);
        }
    }

    /**
     * Property: 多用户加入一致性
     * 验证多个用户加入和离开后，在线人数计数正确
     */
    @Property(tries = 30)
    @Label("多用户加入一致性")
    void multipleUsersJoinConsistency(
            @ForAll @LongRange(min = 1, max = 100) Long baseUserId,
            @ForAll @IntRange(min = 1, max = 5) int userCount
    ) {
        // Given: 创建测试聊天室
        PublicRoom room = PublicRoom.builder()
                .name("MultiUserTest_" + System.currentTimeMillis())
                .description("Test room")
                .maxOnline(100)
                .currentOnline(0)
                .totalMembers(0)
                .status(RoomStatus.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        publicRoomMapper.insert(room);
        Long roomId = room.getId();

        try {
            // When: 多个用户加入
            for (int i = 0; i < userCount; i++) {
                publicRoomService.joinRoom(roomId, baseUserId * 1000 + i);
            }

            // Then: 在线人数应该等于加入的用户数
            PublicRoom afterJoin = publicRoomMapper.selectById(roomId);
            assertEquals(userCount, afterJoin.getCurrentOnline(), "在线人数应该等于加入的用户数");

            // When: 所有用户离开
            for (int i = 0; i < userCount; i++) {
                publicRoomService.leaveRoom(roomId, baseUserId * 1000 + i);
            }

            // Then: 在线人数应该为0
            PublicRoom afterLeave = publicRoomMapper.selectById(roomId);
            assertEquals(0, afterLeave.getCurrentOnline(), "所有用户离开后在线人数应该为0");
        } finally {
            // Cleanup
            publicRoomMapper.deleteById(roomId);
            redisTemplate.delete(ROOM_ONLINE_KEY_PREFIX + roomId);
        }
    }

    /**
     * Property: 关闭的聊天室不能加入
     * 验证已关闭状态的聊天室拒绝用户加入
     */
    @Property(tries = 30)
    @Label("关闭的聊天室不能加入")
    void closedRoomCannotBeJoined(
            @ForAll @LongRange(min = 1, max = 10000) Long userId
    ) {
        // Given: 创建并关闭聊天室
        PublicRoom room = PublicRoom.builder()
                .name("ClosedRoomTest_" + System.currentTimeMillis())
                .description("Test room")
                .maxOnline(100)
                .currentOnline(0)
                .totalMembers(0)
                .status(RoomStatus.CLOSED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        publicRoomMapper.insert(room);
        Long roomId = room.getId();

        try {
            // When & Then: 尝试加入关闭的聊天室应该失败
            assertThrows(Exception.class, () -> publicRoomService.joinRoom(roomId, userId),
                    "加入关闭的聊天室应该抛出异常");
        } finally {
            // Cleanup
            publicRoomMapper.deleteById(roomId);
        }
    }
}
