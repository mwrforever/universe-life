package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.universe.life.user.privacy.domain.dao.query.AdminUserListQuery;
import com.universe.life.user.privacy.domain.dto.AdminUserDTO;
import com.universe.life.user.privacy.domain.po.User;

/**
 * @author 毛伟然
 * @since 2025/12/2 15:55
 */
public interface AdminUserMapper extends BaseMapper<User> {


    /**
     * 获取用户列表
     *
     * @param page 分页参数
     * @param query 查询参数
     * @return 用户列表
     */
    IPage<AdminUserDTO> getAdminUserList(IPage<AdminUserDTO> page, AdminUserListQuery query);
}
