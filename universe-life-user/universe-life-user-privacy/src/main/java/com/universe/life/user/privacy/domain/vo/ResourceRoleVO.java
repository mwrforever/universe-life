package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源角色关联VO
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "资源角色关联VO")
public class ResourceRoleVO {

    @Schema(description = "关联ID")
    private Long id;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "资源编码")
    private String resourceCode;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源类型")
    private Integer resourceType;

    @Schema(description = "授权人ID")
    private Long grantedBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
