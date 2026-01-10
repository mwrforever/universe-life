package com.universe.life.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.universe.life.message.domain.po.Conversation;
import com.universe.life.message.domain.vo.ConversationVO;
import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.enums.ConversationType;
import com.universe.life.message.mapper.ConversationMapper;
import com.universe.life.message.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;

    private static final int MAX_CONTENT_LENGTH = 100;

    @Override
    public List<ConversationVO> getConversationList(Long userId) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getIsDeleted, BooleanFlag.NO)
                .orderByDesc(Conversation::getIsTop)
                .orderByDesc(Conversation::getLastMessageTime);

        List<Conversation> conversations = conversationMapper.selectList(queryWrapper);
        if (conversations == null || conversations.isEmpty()) {
            return Collections.emptyList();
        }

        return conversations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateConversation(Long userId, Long targetId, ConversationType conversationType,
                                           String messageId, String messageContent, LocalDateTime messageTime) {
        Conversation existing = getConversation(userId, targetId, conversationType);

        if (existing != null) {
            LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Conversation::getId, existing.getId())
                    .set(Conversation::getLastMessageId, messageId)
                    .set(Conversation::getLastMessageContent, truncateContent(messageContent))
                    .set(Conversation::getLastMessageTime, messageTime)
                    .set(Conversation::getIsDeleted, BooleanFlag.NO)
                    .set(Conversation::getUpdatedAt, LocalDateTime.now());
            conversationMapper.update(null, updateWrapper);
        } else {
            Conversation conversation = Conversation.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .conversationType(conversationType)
                    .lastMessageId(messageId)
                    .lastMessageContent(truncateContent(messageContent))
                    .lastMessageTime(messageTime)
                    .unreadCount(0)
                    .isTop(BooleanFlag.NO)
                    .isMuted(BooleanFlag.NO)
                    .isDeleted(BooleanFlag.NO)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            conversationMapper.insert(conversation);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUnreadCount(Long userId, Long targetId, ConversationType conversationType) {
        Conversation conversation = getConversation(userId, targetId, conversationType);
        if (conversation != null) {
            LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Conversation::getId, conversation.getId())
                    .setSql("unread_count = unread_count + 1")
                    .set(Conversation::getUpdatedAt, LocalDateTime.now());
            conversationMapper.update(null, updateWrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUnreadCount(Long userId, Long targetId, ConversationType conversationType) {
        Conversation conversation = getConversation(userId, targetId, conversationType);
        if (conversation != null) {
            LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Conversation::getId, conversation.getId())
                    .set(Conversation::getUnreadCount, 0)
                    .set(Conversation::getUpdatedAt, LocalDateTime.now());
            conversationMapper.update(null, updateWrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setConversationTop(Long userId, Long targetId, ConversationType conversationType, boolean isTop) {
        Conversation conversation = getConversation(userId, targetId, conversationType);
        if (conversation != null) {
            LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Conversation::getId, conversation.getId())
                    .set(Conversation::getIsTop, isTop ? BooleanFlag.YES : BooleanFlag.NO)
                    .set(Conversation::getUpdatedAt, LocalDateTime.now());
            conversationMapper.update(null, updateWrapper);
            log.info("Conversation top status changed: userId={}, targetId={}, isTop={}", userId, targetId, isTop);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setConversationMuted(Long userId, Long targetId, ConversationType conversationType, boolean isMuted) {
        Conversation conversation = getConversation(userId, targetId, conversationType);
        if (conversation != null) {
            LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Conversation::getId, conversation.getId())
                    .set(Conversation::getIsMuted, isMuted ? BooleanFlag.YES : BooleanFlag.NO)
                    .set(Conversation::getUpdatedAt, LocalDateTime.now());
            conversationMapper.update(null, updateWrapper);
            log.info("Conversation muted status changed: userId={}, targetId={}, isMuted={}", userId, targetId, isMuted);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, Long targetId, ConversationType conversationType) {
        Conversation conversation = getConversation(userId, targetId, conversationType);
        if (conversation != null) {
            LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Conversation::getId, conversation.getId())
                    .set(Conversation::getIsDeleted, BooleanFlag.YES)
                    .set(Conversation::getUpdatedAt, LocalDateTime.now());
            conversationMapper.update(null, updateWrapper);
            log.info("Conversation deleted: userId={}, targetId={}", userId, targetId);
        }
    }

    @Override
    public int getTotalUnreadCount(Long userId) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getIsDeleted, BooleanFlag.NO)
                .eq(Conversation::getIsMuted, BooleanFlag.NO)
                .select(Conversation::getUnreadCount);

        List<Conversation> conversations = conversationMapper.selectList(queryWrapper);
        if (conversations == null || conversations.isEmpty()) {
            return 0;
        }

        return conversations.stream()
                .mapToInt(c -> c.getUnreadCount() != null ? c.getUnreadCount() : 0)
                .sum();
    }

    // ==================== 私有方法 ====================

    private Conversation getConversation(Long userId, Long targetId, ConversationType conversationType) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getTargetId, targetId)
                .eq(Conversation::getConversationType, conversationType);
        return conversationMapper.selectOne(queryWrapper);
    }

    private ConversationVO convertToVO(Conversation conversation) {
        return ConversationVO.builder()
                .id(conversation.getId())
                .targetId(conversation.getTargetId())
                .conversationType(conversation.getConversationType())
                .lastMessageId(conversation.getLastMessageId())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageTime(conversation.getLastMessageTime())
                .unreadCount(conversation.getUnreadCount())
                .isTop(conversation.getIsTop())
                .isMuted(conversation.getIsMuted())
                .build();
    }

    private String truncateContent(String content) {
        if (content == null) {
            return null;
        }
        return content.length() > MAX_CONTENT_LENGTH ? content.substring(0, MAX_CONTENT_LENGTH) + "..." : content;
    }
}
