package com.universe.life.message.domain.vo;

import com.universe.life.message.enums.BooleanFlag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户聊天设置 VO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户聊天设置")
public class UserChatSettingVO {

    @Schema(description = "允许公共单人会话")
    private BooleanFlag allowPublicMessage;

    @Schema(description = "允许陌生人消息")
    private BooleanFlag allowStrangerMessage;

    @Schema(description = "消息通知")
    private BooleanFlag messageNotification;
}
