package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 资源更新请求
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "资源更新请求")
public class ResourceUpdateRequest {
    @Schema(description = "资源编码", example = "user:create")
    @Size(max = 100, message = "资源编码长度不能超过100")
    private String resourceCode;

    @Schema(description = "资源名称", example = "创建用户接口")
    @Size(max = 100, message = "资源名称长度不能超过100")
    private String resourceName;

    @Schema(description = "资源类型：0 菜单 1 按钮 2 接口 3 数据", example = "2")
    private ResourceType resourceType;

    @Schema(description = "所属微服务名称", example = "user-service")
    @Size(max = 50, message = "服务名称长度不能超过50")
    private String serviceName;

    @Schema(description = "URL路径模式", example = "/api/v2/users")
    @Size(max = 500, message = "URL模式长度不能超过500")
    private String urlPattern;

    @Schema(description = "HTTP方法", example = "POST")
    @Size(max = 10, message = "HTTP方法长度不能超过10")
    private String httpMethod;

    @Schema(description = "父资源ID")
    private Long parentId;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "资源描述")
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
}
