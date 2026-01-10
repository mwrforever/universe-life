package com.universe.life.message.domain.vo;

import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.enums.ConversationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话 VO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话信息")
public class ConversationVO {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "目标名称")
    private String targetName;

    @Schema(description = "目标头像")
    private String targetAvatar;

    @Schema(description = "会话类型")
    private ConversationType conversationType;

    @Schema(description = "最后一条消息ID")
    private String lastMessageId;

    @Schema(description = "最后一条消息内容摘要")
    private String lastMessageContent;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "是否置顶")
    private BooleanFlag isTop;

    @Schema(description = "是否免打扰")
    private BooleanFlag isMuted;
}
