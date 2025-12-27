package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门简单VO（下拉列表/关联展示）
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "部门简单VO")
public class SysDepartmentSimpleVO {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "部门名称")
    private String deptName;
}
