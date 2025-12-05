package com.universe.life.user.privacy.domain.dto;

import com.universe.life.model.enums.UserStatus;
import com.universe.life.user.privacy.enums.Gender;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 毛伟然
 * @since 2025/12/5 10:29
 */
@Data
public class AdminUserDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private Gender gender;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
