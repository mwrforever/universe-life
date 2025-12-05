package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.domain.dto.UserStatusDTO;
import com.universe.life.user.privacy.domain.po.User;

/**
 * 用户服务接口
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface IUserService extends IService<User> {

    /**
     * 添加用户（保留原有接口）
     *
     * @param registerFormDTO 注册表单
     */
    void add(RegisterFormDTO registerFormDTO);

    /**
     * 获取用户状态（保留原有接口）
     *
     * @param username 用户名
     * @return 用户状态
     */
    UserStatusDTO getStatusByUsername(String username);
}
