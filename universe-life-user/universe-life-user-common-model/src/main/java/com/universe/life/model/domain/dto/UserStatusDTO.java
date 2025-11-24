package com.universe.life.model.domain.dto;

import com.universe.life.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/23 20:34
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserStatusDTO {

    private UserStatus status;

}
