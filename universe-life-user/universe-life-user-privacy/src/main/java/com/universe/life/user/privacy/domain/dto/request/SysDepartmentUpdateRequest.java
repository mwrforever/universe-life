package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.CommonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门更新请求
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "部门更新请求")
public class SysDepartmentUpdateRequest {

    @Schema(description = "父部门ID（0为顶级部门）", example = "0")
    private Long parentId;

    @Schema(description = "部门名称", example = "技术部")
    @Size(max = 64, message = "部门名称长度不能超过64")
    private String deptName;

    @Schema(description = "部门负责人ID")
    private Long leaderId;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态：0 禁用 1 启用", example = "1")
    private CommonStatus status;

    @Schema(description = "部门描述")
    @Size(max = 255, message = "描述长度不能超过255")
    private String description;
}
