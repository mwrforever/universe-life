package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.user.privacy.domain.dto.request.RoleIdsRequest;
import com.universe.life.user.privacy.domain.dto.request.UserRoleAssignRequest;
import com.universe.life.user.privacy.domain.dto.request.UserRoleBatchAssignRequest;
import com.universe.life.user.privacy.domain.po.UserRole;
import com.universe.life.user.privacy.domain.vo.BatchResultVO;
import com.universe.life.user.privacy.domain.vo.UserPermissionVO;
import com.universe.life.user.privacy.domain.vo.UserRoleDetailVO;

import java.util.List;

/**
 * 用户-角色关联服务接口
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
public interface IAdminUserRoleService extends IService<UserRole> {

    /**
     * 为用户分配角色
     *
     * @param request 分配请求
     * @return 用户角色详情列表
     */
    List<UserRoleDetailVO> assignRoles(UserRoleAssignRequest request);

    /**
     * 获取用户的角色列表
     *
     * @param userId 用户ID
     * @return 用户角色详情列表
     */
    List<UserRoleDetailVO> getUserRoles(Long userId);

    /**
     * 移除用户的角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void removeUserRole(Long userId, Long roleId);

    /**
     * 批量移除用户的角色
     *
     * @param userId  用户ID
     * @param request 角色ID列表请求
     * @return 移除数量
     */
    Integer batchRemoveUserRoles(Long userId, RoleIdsRequest request);

    /**
     * 更新用户的角色（全量替换）
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void updateUserRoles(Long userId, List<Long> roleIds);

    /**
     * 批量为多个用户分配角色
     *
     * @param request 批量分配请求
     * @return 批量操作结果
     */
    BatchResultVO batchAssignRole(UserRoleBatchAssignRequest request);

    /**
     * 检查用户是否拥有某角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否拥有
     */
    Boolean checkUserRole(Long userId, Long roleId);

    /**
     * 获取用户的所有权限
     *
     * @param userId 用户ID
     * @return 用户权限VO
     */
    UserPermissionVO getUserPermissions(Long userId);
}
