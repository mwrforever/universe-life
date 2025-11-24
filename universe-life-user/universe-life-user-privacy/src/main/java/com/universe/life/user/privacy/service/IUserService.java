package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.domain.dto.UserStatusDTO;
import com.universe.life.user.privacy.domain.po.User;

/**
 * <p>
 * 系统用户表 服务类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface IUserService extends IService<User> {

    /**
     * 添加用户
     *
     * @param registerFormDTO 注册表单
     */
    void add(RegisterFormDTO registerFormDTO);

    /**
     * 获取用户状态
     *
      * @param username 用户名
     * @return 用户状态
     */
    UserStatusDTO getStatusByUsername(String username);
}
