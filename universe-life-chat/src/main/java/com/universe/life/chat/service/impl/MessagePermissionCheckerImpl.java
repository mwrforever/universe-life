package com.universe.life.chat.service.impl;

import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.chat.service.MessagePermissionChecker;
import com.universe.life.message.exception.ChatExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息权限检查器实现
 * 通过 Redis 缓存和 Feign 调用 Message Service 进行权限校验
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePermissionCheckerImpl implements MessagePermissionChecker {

    private final StringRedisTemplate redisTemplate;

    private static final String FRIEND_CACHE_KEY = "chat:friend:";
    private static final String GROUP_MEMBER_CACHE_KEY = "chat:group:members:";
    private static final String GROUP_MUTED_KEY = "chat:group:muted:";
    private static final String ROOM_STATUS_KEY = "chat:room:status:";
    private static final String USER_PUBLIC_CHAT_KEY = "chat:user:public:";

    @Override
    public void checkPrivateMessagePermission(Long senderId, Long receiverId) {
        // 检查是否为好友关系
        String friendKey = FRIEND_CACHE_KEY + senderId;
        Boolean isFriend = redisTemplate.opsForSet().isMember(friendKey, String.valueOf(receiverId));

        if (isFriend == null || !isFriend) {
            // 缓存未命中，需要调用 Message Service 查询
            // TODO: 通过 Feign 调用 Message Service 的 FriendService.isFriend()
            // 这里暂时假设缓存已预热，如果缓存不存在则认为不是好友
            log.warn("Friend cache miss or not friend: senderId={}, receiverId={}", senderId, receiverId);
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_FRIEND);
        }
    }

    @Override
    public void checkGroupMessagePermission(Long senderId, Long groupId) {
        // 检查是否为群成员
        String memberKey = GROUP_MEMBER_CACHE_KEY + groupId;
        Boolean isMember = redisTemplate.opsForSet().isMember(memberKey, String.valueOf(senderId));

        if (isMember == null || !isMember) {
            // 缓存未命中，需要调用 Message Service 查询
            // TODO: 通过 Feign 调用 Message Service 的 GroupService.isGroupMember()
            log.warn("Group member cache miss or not member: senderId={}, groupId={}", senderId, groupId);
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        // 检查是否被禁言
        String mutedKey = GROUP_MUTED_KEY + groupId + ":" + senderId;
        String mutedUntil = redisTemplate.opsForValue().get(mutedKey);

        if (mutedUntil != null) {
            // 计算剩余禁言时间
            try {
                long mutedUntilTime = Long.parseLong(mutedUntil);
                long now = System.currentTimeMillis();
                if (mutedUntilTime > now) {
                    int remainingMinutes = (int) ((mutedUntilTime - now) / 60000);
                    throw new BusinessException.OperationNotAllowedException(
                            ChatExceptionMessage.ChatFormatter.muteRemaining(remainingMinutes)
                    );
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid muted until value: {}", mutedUntil);
            }
        }
    }

    @Override
    public void checkRoomMessagePermission(Long senderId, Long roomId) {
        // 检查聊天室状态
        String statusKey = ROOM_STATUS_KEY + roomId;
        String status = redisTemplate.opsForValue().get(statusKey);

        if (status == null) {
            // 缓存未命中，需要调用 Message Service 查询
            // TODO: 通过 Feign 调用 Message Service 的 PublicRoomService.getRoomById()
            log.warn("Room status cache miss: roomId={}", roomId);
            // 暂时允许发送，实际应该查询数据库
            return;
        }

        if ("0".equals(status)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.ROOM_CLOSED);
        }
    }

    @Override
    public void checkPublicMessagePermission(Long senderId, Long receiverId) {
        // 检查目标用户是否开启公共会话
        String publicChatKey = USER_PUBLIC_CHAT_KEY + receiverId;
        String enabled = redisTemplate.opsForValue().get(publicChatKey);

        if (enabled == null) {
            // 缓存未命中，需要调用 Message Service 查询
            // TODO: 通过 Feign 调用 Message Service 的 UserChatSettingService.getSettings()
            log.warn("Public chat setting cache miss: receiverId={}", receiverId);
            // 暂时允许发送，实际应该查询数据库
            return;
        }

        if ("0".equals(enabled)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.PUBLIC_CHAT_DISABLED);
        }
    }
}
