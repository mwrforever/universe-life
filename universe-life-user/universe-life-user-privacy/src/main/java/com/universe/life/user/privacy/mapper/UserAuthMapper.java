package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.domain.dto.request.PasswordUserRequest;
import com.universe.life.user.privacy.domain.po.UserAuth;

/**
 * <p>
 * 用户认证表 Mapper 接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface UserAuthMapper extends BaseMapper<UserAuth> {

    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoDTO getUserInfo(String username);

    /**
     * 获取用户密码
     *
     * @param userId 用户id
     * @return 用户密码
     */
    PasswordUserRequest getPasswordByUserId(Long userId);
}
