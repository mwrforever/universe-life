package com.universe.life.message.domain.vo;

import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.enums.GroupMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群组成员 VO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "群组成员信息")
public class GroupMemberVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "群内昵称")
    private String nickname;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "角色")
    private GroupMemberRole role;

    @Schema(description = "是否被禁言")
    private BooleanFlag muted;

    @Schema(description = "禁言截止时间")
    private LocalDateTime mutedUntil;

    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;
}
