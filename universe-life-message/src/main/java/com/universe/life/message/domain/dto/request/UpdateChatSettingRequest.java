package com.universe.life.message.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新聊天设置 Request
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新聊天设置请求")
public class UpdateChatSettingRequest {

    @Schema(description = "允许公共单人会话")
    private Boolean allowPublicMessage;

    @Schema(description = "允许陌生人消息")
    private Boolean allowStrangerMessage;

    @Schema(description = "消息通知")
    private Boolean messageNotification;
}
