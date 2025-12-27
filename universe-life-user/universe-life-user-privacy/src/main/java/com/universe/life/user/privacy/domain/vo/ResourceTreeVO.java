package com.universe.life.user.privacy.domain.vo;

import com.universe.life.user.privacy.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 资源树VO
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "资源树VO")
public class ResourceTreeVO {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "资源编码")
    private String resourceCode;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源类型")
    private ResourceType resourceType;

    @Schema(description = "是否选中（用于角色权限分配）")
    private Boolean checked;

    @Schema(description = "子资源列表")
    private List<ResourceTreeVO> children;
}
