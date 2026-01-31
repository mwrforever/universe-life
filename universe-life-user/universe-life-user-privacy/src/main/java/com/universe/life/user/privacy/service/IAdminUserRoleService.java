package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.user.privacy.domain.dto.request.RoleIdsRequest;
import com.universe.life.user.privacy.domain.dto.request.UserRoleAssignRequest;
import com.universe.life.user.privacy.domain.po.UserRole;
import com.universe.life.user.privacy.domain.vo.RoleAssignmentStatusVO;
import com.universe.life.user.privacy.domain.vo.UserPermissionVO;

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
     */
    void assignRoles(UserRoleAssignRequest request);

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
     */
    void batchRemoveUserRoles(Long userId, RoleIdsRequest request);

    /**
     * 获取用户的所有权限
     *
     * @param userId 用户ID
     * @return 用户权限VO
     */
    UserPermissionVO getUserPermissions(Long userId);

    /**
     * 获取用户的角色分配状态
     * 返回已分配和未分配的角色列表
     *
     * @param userId 用户ID
     * @return 角色分配状态VO
     */
    RoleAssignmentStatusVO getRoleAssignmentStatus(Long userId);
}
