package com.universe.life.user.privacy.domain.vo;

import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台员工详情VO
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "平台员工详情VO")
public class SysUserDetailVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "性别")
    private Gender gender;

    @Schema(description = "状态")
    private CommonStatus status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "所属部门列表")
    private List<SysDepartmentSimpleVO> departments;

    @Schema(description = "主部门")
    private SysDepartmentSimpleVO primaryDepartment;
}
