package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.user.privacy.domain.dao.UserInfoDO;
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
    UserInfoDO getUserInfo(String username);
}
