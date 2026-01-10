package com.universe.life.message.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建群组 Request
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建群组请求")
public class CreateGroupRequest {

    @NotBlank(message = "群组名称不能为空")
    @Size(max = 50, message = "群组名称不能超过50字")
    @Schema(description = "群组名称", required = true)
    private String name;

    @Schema(description = "群组头像URL")
    private String avatar;

    @Size(max = 500, message = "群组描述不能超过500字")
    @Schema(description = "群组描述")
    private String description;

    @Schema(description = "是否公开群", defaultValue = "false")
    private Boolean isPublic;

    @Schema(description = "允许成员邀请", defaultValue = "true")
    private Boolean allowMemberInvite;

    @Schema(description = "初始邀请的成员ID列表")
    private List<Long> memberIds;
}
