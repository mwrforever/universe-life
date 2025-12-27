package com.universe.life.user.privacy.domain.dao.query;

import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 角色分页查询对象
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Data
@Schema(description = "角色分页查询对象")
public class RoleListQuery {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;

    @Schema(description = "角色类型筛选")
    private RoleType roleType;

    @Schema(description = "状态筛选")
    private CommonStatus status;

    @Schema(description = "关键词搜索（角色编码/名称）")
    private String keyword;
}
