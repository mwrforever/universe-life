package com.universe.life.user.privacy.service;

import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户认证表 服务类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-03
 */
public interface IUserAuthService extends IService<UserAuth> {

    /**
     * 获取密码
     *
     * @param username 用户名
     * @return 密码
     */
    UserInfoDTO getUserInfo(String username);
}
