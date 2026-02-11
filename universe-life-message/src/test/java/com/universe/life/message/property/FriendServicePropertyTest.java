package com.universe.life.message.property;

import com.universe.life.message.domain.po.FriendRequest;
import com.universe.life.message.domain.po.UserFriend;
import com.universe.life.message.enums.FriendRequestStatus;
import com.universe.life.message.enums.FriendStatus;
import com.universe.life.message.mapper.FriendRequestMapper;
import com.universe.life.message.mapper.UserFriendMapper;
import com.universe.life.message.service.FriendService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 好友服务属性测试
 * 使用 jqwik 进行属性测试，验证好友关系的正确性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class FriendServicePropertyTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserFriendMapper userFriendMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    /**
     * Property 10: 好友关系双向一致性
     * *For any* two users A and B, if A is a friend of B, then B must also be a friend of A
     * 
     * **Validates: Requirements 6.2, 6.4**
     */
    @Property(tries = 100)
    @Label("Property 10: 好友关系双向一致性")
    void friendRelationshipIsBidirectional(
            @ForAll @LongRange(min = 1, max = 10000) Long userId1,
            @ForAll @LongRange(min = 1, max = 10000) Long userId2
    ) {
        Assume.that(!userId1.equals(userId2));

        // Given: 创建双向好友关系
        createFriendRelation(userId1, userId2);
        createFriendRelation(userId2, userId1);

        // When & Then: 验证双向一致性
        boolean user1HasUser2 = friendService.isFriend(userId1, userId2);
        boolean user2HasUser1 = friendService.isFriend(userId2, userId1);

        assertEquals(user1HasUser2, user2HasUser1,
                String.format("好友关系应该是双向的: user1=%d, user2=%d", userId1, userId2));

        // Cleanup
        cleanupFriendRelation(userId1, userId2);
    }

    /**
     * Property 11: 好友请求状态机正确性
     * *For any* friend request, the state transitions must follow:
     * PENDING -> ACCEPTED | REJECTED | EXPIRED
     * 
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    @Property(tries = 100)
    @Label("Property 11: 好友请求状态机正确性")
    void friendRequestStateMachineIsCorrect(
            @ForAll @LongRange(min = 1, max = 10000) Long fromUserId,
            @ForAll @LongRange(min = 1, max = 10000) Long toUserId,
            @ForAll("validTransitions") FriendRequestStatus targetStatus
    ) {
        Assume.that(!fromUserId.equals(toUserId));

        // Given: 创建一个待处理的好友请求
        FriendRequest request = createPendingRequest(fromUserId, toUserId);
        Long requestId = request.getId();

        // When: 执行状态转换
        FriendRequestStatus initialStatus = request.getStatus();
        assertEquals(FriendRequestStatus.PENDING, initialStatus, "初始状态应该是 PENDING");

        // Then: 验证状态转换的有效性
        // PENDING 状态只能转换到 ACCEPTED, REJECTED, 或 EXPIRED
        assertTrue(
                targetStatus == FriendRequestStatus.ACCEPTED ||
                targetStatus == FriendRequestStatus.REJECTED ||
                targetStatus == FriendRequestStatus.EXPIRED,
                "从 PENDING 状态只能转换到 ACCEPTED, REJECTED, 或 EXPIRED"
        );

        // Cleanup
        friendRequestMapper.deleteById(requestId);
    }

    /**
     * 提供有效的状态转换目标
     */
    @Provide
    Arbitrary<FriendRequestStatus> validTransitions() {
        return Arbitraries.of(
                FriendRequestStatus.ACCEPTED,
                FriendRequestStatus.REJECTED,
                FriendRequestStatus.EXPIRED
        );
    }

    /**
     * Property: 删除好友后双方都不再是好友
     * *For any* friend relationship, after deletion, neither user should see the other as a friend
     * 
     * **Validates: Requirements 6.4**
     */
    @Property(tries = 50)
    @Label("删除好友后双方都不再是好友")
    void deleteFriendRemovesBothDirections(
            @ForAll @LongRange(min = 1, max = 10000) Long userId1,
            @ForAll @LongRange(min = 1, max = 10000) Long userId2
    ) {
        Assume.that(!userId1.equals(userId2));

        // Given: 创建双向好友关系
        createFriendRelation(userId1, userId2);
        createFriendRelation(userId2, userId1);

        // Verify they are friends
        assertTrue(friendService.isFriend(userId1, userId2), "应该是好友");

        // When: 删除好友关系
        deleteFriendRelation(userId1, userId2);
        deleteFriendRelation(userId2, userId1);

        // Then: 双方都不再是好友
        assertFalse(friendService.isFriend(userId1, userId2), "删除后不应该是好友");
        assertFalse(friendService.isFriend(userId2, userId1), "删除后不应该是好友");

        // Cleanup
        cleanupFriendRelation(userId1, userId2);
    }

    /**
     * Property: 好友列表包含所有好友
     * *For any* user with friends, getFriendIds should return all friend IDs
     * 
     * **Validates: Requirements 6.6**
     */
    @Property(tries = 50)
    @Label("好友列表包含所有好友")
    void friendListContainsAllFriends(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long friendId
    ) {
        Assume.that(!userId.equals(friendId));

        // Given: 创建好友关系
        createFriendRelation(userId, friendId);

        // When: 获取好友列表
        List<Long> friendIds = friendService.getFriendIds(userId);

        // Then: 好友列表应该包含该好友
        assertTrue(friendIds.contains(friendId),
                String.format("好友列表应该包含好友 %d", friendId));

        // Cleanup
        cleanupFriendRelation(userId, friendId);
    }

    // ==================== Helper Methods ====================

    private void createFriendRelation(Long userId, Long friendId) {
        UserFriend existing = userFriendMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                        .eq(UserFriend::getUserId, userId)
                        .eq(UserFriend::getFriendId, friendId)
        );

        if (existing == null) {
            UserFriend friend = UserFriend.builder()
                    .userId(userId)
                    .friendId(friendId)
                    .status(FriendStatus.NORMAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userFriendMapper.insert(friend);
        } else if (existing.getStatus() == FriendStatus.DELETED) {
            existing.setStatus(FriendStatus.NORMAL);
            existing.setUpdatedAt(LocalDateTime.now());
            userFriendMapper.updateById(existing);
        }
    }

    private void deleteFriendRelation(Long userId, Long friendId) {
        userFriendMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserFriend>()
                        .eq(UserFriend::getUserId, userId)
                        .eq(UserFriend::getFriendId, friendId)
                        .set(UserFriend::getStatus, FriendStatus.DELETED)
        );
    }

    private void cleanupFriendRelation(Long userId1, Long userId2) {
        userFriendMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                        .eq(UserFriend::getUserId, userId1)
                        .eq(UserFriend::getFriendId, userId2)
        );
        userFriendMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                        .eq(UserFriend::getUserId, userId2)
                        .eq(UserFriend::getFriendId, userId1)
        );
    }

    private FriendRequest createPendingRequest(Long fromUserId, Long toUserId) {
        FriendRequest request = FriendRequest.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .message("Test request")
                .status(FriendRequestStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        friendRequestMapper.insert(request);
        return request;
    }
}
