package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.domain.po.UserAuth;

/**
 * <p>
 * 用户认证表 服务类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface IUserAuthService extends IService<UserAuth> {

    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoDTO getUserInfo(String username);
}
