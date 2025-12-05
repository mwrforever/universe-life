package com.universe.life.user.privacy.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 毛伟然
 * @since 2025/12/5 15:47
 */
@Data
public class PasswordUserRequest {
    @NotBlank(message = "密码不能为空")
    private String password;
}
