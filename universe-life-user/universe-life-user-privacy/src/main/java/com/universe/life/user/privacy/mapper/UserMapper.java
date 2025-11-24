package com.universe.life.user.privacy.mapper;

import com.universe.life.user.privacy.domain.dao.UserStatusDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 系统用户表 Mapper 接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户状态
     *
     * @param username 用户名
     * @return 用户状态
     */
    UserStatusDo selectUserStatusByUsername(String username);
}
