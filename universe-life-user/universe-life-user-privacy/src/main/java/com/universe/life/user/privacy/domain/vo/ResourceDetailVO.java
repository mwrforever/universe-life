package com.universe.life.user.privacy.domain.vo;

import com.universe.life.user.privacy.enums.ResourceStatus;
import com.universe.life.user.privacy.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源详情VO
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "资源详情VO")
public class ResourceDetailVO {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "资源编码")
    private String resourceCode;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "资源类型")
    private ResourceType resourceType;

    @Schema(description = "所属微服务名称")
    private String serviceName;

    @Schema(description = "URL路径模式")
    private String urlPattern;

    @Schema(description = "HTTP方法")
    private String httpMethod;

    @Schema(description = "父资源ID")
    private Long parentId;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "状态")
    private ResourceStatus status;

    @Schema(description = "资源描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
