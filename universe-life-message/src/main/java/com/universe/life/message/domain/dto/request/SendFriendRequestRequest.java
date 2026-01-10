package com.universe.life.message.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送好友请求 Request
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送好友请求")
public class SendFriendRequestRequest {

    @NotNull(message = "目标用户ID不能为空")
    @Schema(description = "目标用户ID", required = true)
    private Long toUserId;

    @Size(max = 200, message = "申请消息不能超过200字")
    @Schema(description = "申请消息/验证信息")
    private String message;
}
