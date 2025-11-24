package com.universe.life.user.privacy.domain.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 毛伟然
 * @since 2025/11/24 13:32
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserInfoDO {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
}
