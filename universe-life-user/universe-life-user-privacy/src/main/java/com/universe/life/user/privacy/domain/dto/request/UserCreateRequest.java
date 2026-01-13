package com.universe.life.user.privacy.domain.dto.request;

import com.universe.life.model.enums.UserStatus;
import com.universe.life.user.privacy.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户创建请求对象
 *
 * @author Claude
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户创建请求对象")
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "性别", example = "MALE")
    private Gender gender;

    @Schema(description = "用户状态", example = "NORMAL")
    private UserStatus status;

    @NotNull(message = "认证信息列表不能为空")
    @Schema(description = "认证信息列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UserAuthRequest> userAuthList;


    @Schema(description = "创建时间", example = "2025-12-02 12:00:00")
    @NotNull(message = "创建时间不能为空")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-12-02 12:00:00")
    @NotNull(message = "更新时间不能为空")
    private LocalDateTime updatedAt;
}