package com.universe.life.model.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/4 14:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private String username;

    private String password;

    private Long userId;

}
