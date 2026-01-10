package com.universe.life.chat.domain.dto.request;

import com.universe.life.message.enums.ContentType;
import lombok.Data;

/**
 * 群聊消息请求
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
public class GroupMessageRequest {

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 内容类型
     */
    private ContentType contentType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 扩展信息（JSON格式）
     */
    private String extra;

    /**
     * 客户端消息ID（用于幂等处理）
     */
    private String clientMessageId;
}
