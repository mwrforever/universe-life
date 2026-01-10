package com.universe.life.message.service;

import com.universe.life.message.domain.dto.ChatMessageDTO;
import com.universe.life.message.domain.vo.ChatMessageVO;
import com.universe.life.message.enums.MessageType;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 消息持久化服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface MessagePersistenceService {

    /**
     * 保存消息
     *
     * @param message 消息DTO
     * @return 保存后的消息VO
     */
    ChatMessageVO saveMessage(ChatMessageDTO message);

    /**
     * 批量保存消息
     *
     * @param messages 消息列表
     */
    void saveMessages(List<ChatMessageDTO> messages);

    /**
     * 查询历史消息
     *
     * @param userId      当前用户ID
     * @param targetId    目标ID
     * @param messageType 消息类型
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @return 消息分页
     */
    Page<ChatMessageVO> getHistoryMessages(Long userId, Long targetId, MessageType messageType, int pageNum, int pageSize);

    /**
     * 同步消息（消息漫游）
     *
     * @param userId       用户ID
     * @param lastSequence 上次同步的序列号
     * @param limit        最大同步数量
     * @return 消息列表
     */
    List<ChatMessageVO> syncMessages(Long userId, Long lastSequence, int limit);

    /**
     * 根据消息ID获取消息
     *
     * @param messageId 消息ID
     * @return 消息VO
     */
    ChatMessageVO getMessageById(String messageId);

    /**
     * 检查消息是否已处理（幂等检查）
     *
     * @param clientMessageId 客户端消息ID
     * @return 是否已处理
     */
    boolean isMessageProcessed(String clientMessageId);

    /**
     * 生成消息ID
     *
     * @return 消息ID
     */
    String generateMessageId();

    /**
     * 生成消息序列号
     *
     * @param userId 用户ID
     * @return 序列号
     */
    Long generateSequence(Long userId);
}
