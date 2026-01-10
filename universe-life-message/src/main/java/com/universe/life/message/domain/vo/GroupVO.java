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
 * 群组信息 VO
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "群组信息")
public class GroupVO {

    @Schema(description = "群组ID")
    private Long id;

    @Schema(description = "群组名称")
    private String name;

    @Schema(description = "群组头像")
    private String avatar;

    @Schema(description = "群组描述")
    private String description;

    @Schema(description = "群主ID")
    private Long ownerId;

    @Schema(description = "群聊码")
    private String groupCode;

    @Schema(description = "是否公开群")
    private BooleanFlag isPublic;

    @Schema(description = "允许成员邀请")
    private BooleanFlag allowMemberInvite;

    @Schema(description = "最大成员数")
    private Integer maxMembers;

    @Schema(description = "当前成员数")
    private Integer memberCount;

    @Schema(description = "当前用户在群中的角色")
    private GroupMemberRole myRole;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
