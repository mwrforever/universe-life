package com.universe.life.message.service;

import com.universe.life.message.domain.dto.ChatMessageDTO;

import java.util.List;

/**
 * 离线消息服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface OfflineMessageService {

    /**
     * 存储离线消息
     *
     * @param userId  用户ID
     * @param message 消息
     */
    void storeOfflineMessage(Long userId, ChatMessageDTO message);

    /**
     * 获取并清除离线消息
     *
     * @param userId 用户ID
     * @return 离线消息列表
     */
    List<ChatMessageDTO> fetchAndClearOfflineMessages(Long userId);

    /**
     * 获取离线消息（不清除）
     *
     * @param userId 用户ID
     * @return 离线消息列表
     */
    List<ChatMessageDTO> getOfflineMessages(Long userId);

    /**
     * 获取离线消息数量
     *
     * @param userId 用户ID
     * @return 离线消息数量
     */
    int getOfflineMessageCount(Long userId);

    /**
     * 清除离线消息
     *
     * @param userId 用户ID
     */
    void clearOfflineMessages(Long userId);
}
