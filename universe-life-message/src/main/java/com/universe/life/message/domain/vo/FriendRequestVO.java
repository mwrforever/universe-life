package com.universe.life.message.domain.vo;

import com.universe.life.message.enums.FriendRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友请求 VO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "好友请求信息")
public class FriendRequestVO {

    @Schema(description = "请求ID")
    private Long id;

    @Schema(description = "申请人ID")
    private Long fromUserId;

    @Schema(description = "申请人昵称")
    private String fromUserNickname;

    @Schema(description = "申请人头像")
    private String fromUserAvatar;

    @Schema(description = "申请消息")
    private String message;

    @Schema(description = "状态")
    private FriendRequestStatus status;

    @Schema(description = "申请时间")
    private LocalDateTime createdAt;

    @Schema(description = "过期时间")
    private LocalDateTime expiredAt;
}
