package com.universe.life.message.service.impl;

import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.message.domain.document.ChatMessageDocument;
import com.universe.life.message.domain.dto.ChatMessageDTO;
import com.universe.life.message.domain.vo.ChatMessageVO;
import com.universe.life.message.enums.MessageStatus;
import com.universe.life.message.enums.MessageType;
import com.universe.life.message.exception.ChatExceptionMessage;
import com.universe.life.message.mapstruct.ChatMessageMapStruct;
import com.universe.life.message.repository.ChatMessageRepository;
import com.universe.life.message.service.MessagePersistenceService;
import com.universe.life.message.util.MessageEncryptionService;
import com.universe.life.message.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息持久化服务实现
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePersistenceServiceImpl implements MessagePersistenceService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapStruct chatMessageMapStruct;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final MessageEncryptionService encryptionService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String SEQUENCE_KEY_PREFIX = "chat:sequence:";

    @Override
    public ChatMessageVO saveMessage(ChatMessageDTO message) {
        // 幂等检查
        if (message.getClientMessageId() != null && isMessageProcessed(message.getClientMessageId())) {
            log.info("消息已处理，跳过: clientMessageId={}", message.getClientMessageId());
            return chatMessageRepository.findByClientMessageId(message.getClientMessageId())
                    .map(this::decryptAndConvertToVo)
                    .orElse(null);
        }

        // 生成消息ID和序列号
        if (message.getMessageId() == null) {
            message.setMessageId(generateMessageId());
        }
        if (message.getSequence() == null) {
            message.setSequence(generateSequence(message.getSenderId()));
        }
        if (message.getStatus() == null) {
            message.setStatus(MessageStatus.SENT);
        }
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }

        // 转换为 Document
        ChatMessageDocument document = chatMessageMapStruct.dtoToDocument(message);
        document.setUpdatedAt(LocalDateTime.now());

        // 加密消息内容
        if (message.getContent() != null) {
            document.setContent(encryptionService.encrypt(message.getContent()));
            document.setEncrypted(true);
        } else {
            document.setEncrypted(false);
        }

        // 保存到 MongoDB
        ChatMessageDocument saved = chatMessageRepository.save(document);
        log.debug("消息已保存: messageId={}", saved.getId());

        return decryptAndConvertToVo(saved);
    }

    @Override
    public void saveMessages(List<ChatMessageDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        messages.forEach(this::saveMessage);
    }


    @Override
    public Page<ChatMessageVO> getHistoryMessages(Long userId, Long targetId, MessageType messageType, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ChatMessageDocument> page = switch (messageType) {
            case PRIVATE -> chatMessageRepository.findPrivateMessages(userId, targetId, messageType, pageable);
            case GROUP -> chatMessageRepository.findByGroupIdAndMessageTypeOrderByCreatedAtDesc(targetId, messageType, pageable);
            case ROOM -> chatMessageRepository.findByRoomIdAndMessageTypeOrderByCreatedAtDesc(targetId, messageType, pageable);
            case PUBLIC -> chatMessageRepository.findPublicMessages(userId, targetId, messageType, pageable);
        };

        return page.map(this::decryptAndConvertToVo);
    }

    @Override
    public List<ChatMessageVO> syncMessages(Long userId, Long lastSequence, int limit) {
        List<ChatMessageDocument> documents = chatMessageRepository
                .findBySenderIdAndSequenceGreaterThanOrderBySequenceAsc(userId, lastSequence);

        return documents.stream()
                .limit(limit)
                .map(this::decryptAndConvertToVo)
                .collect(Collectors.toList());
    }

    @Override
    public ChatMessageVO getMessageById(String messageId) {
        return chatMessageRepository.findById(messageId)
                .map(this::decryptAndConvertToVo)
                .orElseThrow(() -> new BusinessException.DataNotFoundException(ChatExceptionMessage.MESSAGE_NOT_FOUND));
    }

    @Override
    public boolean isMessageProcessed(String clientMessageId) {
        if (clientMessageId == null || clientMessageId.isEmpty()) {
            return false;
        }
        return chatMessageRepository.findByClientMessageId(clientMessageId).isPresent();
    }

    @Override
    public String generateMessageId() {
        return snowflakeIdGenerator.nextIdStr();
    }

    @Override
    public Long generateSequence(Long userId) {
        String key = SEQUENCE_KEY_PREFIX + userId;
        Long sequence = stringRedisTemplate.opsForValue().increment(key);
        return sequence != null ? sequence : 1L;
    }

    /**
     * 解密消息内容并转换为 VO
     */
    private ChatMessageVO decryptAndConvertToVo(ChatMessageDocument document) {
        ChatMessageVO vo = chatMessageMapStruct.documentToVo(document);
        // 解密消息内容
        if (Boolean.TRUE.equals(document.getEncrypted()) && document.getContent() != null) {
            vo.setContent(encryptionService.decrypt(document.getContent()));
        }
        return vo;
    }
}
