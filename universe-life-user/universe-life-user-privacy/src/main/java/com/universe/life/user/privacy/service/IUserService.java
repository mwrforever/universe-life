package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.model.domain.dto.UserStatusDTO;
import com.universe.life.user.privacy.domain.dto.request.RegisterFormRequest;
import com.universe.life.user.privacy.domain.dto.request.UserProfileUpdateRequest;
import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.domain.vo.UserInfoVO;

/**
 * 用户服务接口
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface IUserService extends IService<User> {

    /**
     * 获取用户状态（保留原有接口）
     *
     * @param username 用户名
     * @return 用户状态
     */
    UserStatusDTO getStatusByUsername(String username);

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserInfoVO getUserById(Long userId);

    /**
     * 更新用户个人信息
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 用户信息VO
     */
    UserInfoVO updateUserProfile(Long userId, UserProfileUpdateRequest request);

    /**
     * 注册用户
     *
     * @param request 注册请求
     */
    void register(RegisterFormRequest request);
}
