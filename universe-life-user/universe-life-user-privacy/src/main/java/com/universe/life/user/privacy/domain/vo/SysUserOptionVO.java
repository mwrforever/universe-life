package com.universe.life.user.privacy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 平台员工选项VO（下拉列表）
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "平台员工选项VO")
public class SysUserOptionVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "真实姓名")
    private String realName;
}
