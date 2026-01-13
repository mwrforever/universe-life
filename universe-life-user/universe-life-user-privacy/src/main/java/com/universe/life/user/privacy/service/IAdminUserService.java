package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.common.domain.PageResult;
import com.universe.life.user.privacy.domain.dao.query.AdminUserListQuery;
import com.universe.life.user.privacy.domain.dto.request.*;
import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.domain.vo.AdminUserDetailVO;
import com.universe.life.user.privacy.domain.vo.AdminUserListVO;
import com.universe.life.user.privacy.domain.vo.UserStatusVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 管理员用户服务接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
public interface IAdminUserService extends IService<User> {

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 用户创建响应VO
     */
    AdminUserListVO createUser(UserCreateRequest request);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情VO（包含角色信息）
     */
    AdminUserDetailVO getUserById(Long id);

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<User> pageUsers(AdminUserListQuery query);

    /**
     * 更新用户信息
     *
     * @param request 更新请求
     */
    void updateUser(Long id, UserUpdateRequest request);

    /**
     * 删除用户（软删除）
     *
     * @param id 用户ID
     */
    void deleteUser(Long id, PasswordUserRequest request);

    /**
     * 获取用户状态
     *
     * @param username 用户名
     * @return 用户状态VO
     */
    UserStatusVO getUserStatus(String username);

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     */
    void resetPassword(Long id, ResetPasswordRequest request);

    /**
     * 更新用户状态
     *
     * @param request 更新请求
     */
    void updateUserStatus(UserStatusUpdateRequest request);

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     */
    void batchDeleteUsers(List<Long> userIds, PasswordUserRequest request);

    /**
     * 批量更新用户状态
     *
     * @param request 批量更新请求
     */
    void batchUpdateUserStatus(UserBatchStatusUpdateRequest request);


    /**
     * 校验密码
     *
     * @param request 密码请求
     * @return 是否通过校验
     */
    Boolean checkPassword(@Valid PasswordUserRequest request);
}
