package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 平台员工更新请求
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Data
@Schema(description = "平台员工更新请求")
public class SysUserUpdateRequest {

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 64, message = "真实姓名长度不能超过64")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128")
    private String email;

    @Schema(description = "头像URL")
    @Size(max = 255, message = "头像URL长度不能超过255")
    private String avatarUrl;

    @Schema(description = "性别：0 保密 1 男 2 女", example = "1")
    private Gender gender;

    @Schema(description = "状态：0 禁用 1 启用", example = "1")
    private CommonStatus status;

    @Schema(description = "部门ID列表")
    private List<Long> departmentIds;

    @Schema(description = "主部门ID")
    private Long primaryDepartmentId;
}
