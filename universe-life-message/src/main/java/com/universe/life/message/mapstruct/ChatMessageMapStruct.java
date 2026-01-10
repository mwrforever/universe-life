package com.universe.life.message.mapstruct;

import com.universe.life.message.domain.document.ChatMessageDocument;
import com.universe.life.message.domain.dto.ChatMessageDTO;
import com.universe.life.message.domain.vo.ChatMessageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 聊天消息对象转换器
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Mapper(componentModel = "spring")
public interface ChatMessageMapStruct {

    ChatMessageMapStruct INSTANCE = Mappers.getMapper(ChatMessageMapStruct.class);

    /**
     * DTO 转 Document
     */
    @Mapping(source = "messageId", target = "id")
    ChatMessageDocument dtoToDocument(ChatMessageDTO dto);

    /**
     * Document 转 DTO
     */
    @Mapping(source = "id", target = "messageId")
    ChatMessageDTO documentToDto(ChatMessageDocument document);

    /**
     * Document 转 VO
     */
    @Mapping(source = "id", target = "messageId")
    ChatMessageVO documentToVo(ChatMessageDocument document);

    /**
     * Document 列表转 VO 列表
     */
    List<ChatMessageVO> documentsToVos(List<ChatMessageDocument> documents);

    /**
     * DTO 转 VO
     */
    ChatMessageVO dtoToVo(ChatMessageDTO dto);

    /**
     * DTO 列表转 VO 列表
     */
    List<ChatMessageVO> dtosToVos(List<ChatMessageDTO> dtos);
}
