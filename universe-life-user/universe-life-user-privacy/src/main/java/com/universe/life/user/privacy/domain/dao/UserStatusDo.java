package com.universe.life.user.privacy.domain.dao;

import com.universe.life.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/23 20:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusDo {

    private UserStatus status;

}
