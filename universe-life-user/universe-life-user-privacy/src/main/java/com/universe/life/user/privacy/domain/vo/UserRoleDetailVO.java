package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色详情VO
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "用户角色详情VO")
public class UserRoleDetailVO {

    @Schema(description = "关联ID")
    private Long id;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "授权人ID")
    private Long grantedBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
