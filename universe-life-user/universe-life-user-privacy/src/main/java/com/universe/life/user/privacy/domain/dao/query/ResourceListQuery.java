package com.universe.life.user.privacy.domain.dao.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 资源分页查询对象
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "资源分页查询对象")
public class ResourceListQuery {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 110, message = "每页大小不能超过100")
    private Integer size = 10;

    @Schema(description = "资源类型筛选: 0-菜单, 1-按钮, 2-接口, 3-数据权限")
    private Integer resourceType;

    @Schema(description = "微服务名称筛选")
    private String serviceName;

    @Schema(description = "状态筛选: 0-禁用, 1-启用")
    private Integer status;

    @Schema(description = "关键词搜索（资源编码/名称）")
    private String keyword;
}
