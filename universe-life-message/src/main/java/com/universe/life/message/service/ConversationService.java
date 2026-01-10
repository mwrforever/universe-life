package com.universe.life.message.service;

import com.universe.life.message.domain.vo.ConversationVO;
import com.universe.life.message.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface ConversationService {

    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ConversationVO> getConversationList(Long userId);

    /**
     * 创建或更新会话
     *
     * @param userId           用户ID
     * @param targetId         目标ID
     * @param conversationType 会话类型
     * @param messageId        消息ID
     * @param messageContent   消息内容摘要
     * @param messageTime      消息时间
     */
    void createOrUpdateConversation(Long userId, Long targetId, ConversationType conversationType,
                                    String messageId, String messageContent, LocalDateTime messageTime);

    /**
     * 增加未读消息数
     *
     * @param userId           用户ID
     * @param targetId         目标ID
     * @param conversationType 会话类型
     */
    void incrementUnreadCount(Long userId, Long targetId, ConversationType conversationType);

    /**
     * 清除未读消息数
     *
     * @param userId           用户ID
     * @param targetId         目标ID
     * @param conversationType 会话类型
     */
    void clearUnreadCount(Long userId, Long targetId, ConversationType conversationType);

    /**
     * 设置会话置顶
     *
     * @param userId           用户ID
     * @param targetId         目标ID
     * @param conversationType 会话类型
     * @param isTop            是否置顶
     */
    void setConversationTop(Long userId, Long targetId, ConversationType conversationType, boolean isTop);

    /**
     * 设置会话免打扰
     *
     * @param userId           用户ID
     * @param targetId         目标ID
     * @param conversationType 会话类型
     * @param isMuted          是否免打扰
     */
    void setConversationMuted(Long userId, Long targetId, ConversationType conversationType, boolean isMuted);

    /**
     * 删除会话
     *
     * @param userId           用户ID
     * @param targetId         目标ID
     * @param conversationType 会话类型
     */
    void deleteConversation(Long userId, Long targetId, ConversationType conversationType);

    /**
     * 获取用户总未读消息数
     *
     * @param userId 用户ID
     * @return 总未读数
     */
    int getTotalUnreadCount(Long userId);
}
