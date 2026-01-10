package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.user.privacy.domain.dto.request.ResourceIdsRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceRoleAssignRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceRoleBatchAssignRequest;
import com.universe.life.user.privacy.domain.po.ResourceRole;
import com.universe.life.user.privacy.domain.vo.BatchResultVO;
import com.universe.life.user.privacy.domain.vo.ResourceRoleVO;
import com.universe.life.user.privacy.domain.vo.ResourceTreeVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;

import java.util.List;

/**
 * 资源角色关联服务接口
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
public interface IAdminResourceRoleService extends IService<ResourceRole> {

    /**
     * 为角色分配资源权限
     *
     * @param request 分配请求
     */
    void assignResources(ResourceRoleAssignRequest request);

    /**
     * 获取角色的资源权限列表
     *
     * @param roleId 角色ID
     * @return 资源角色关联列表
     */
    List<ResourceRoleVO> getRoleResources(Long roleId);

    /**
     * 移除角色的资源权限
     *
     * @param roleId     角色ID
     * @param resourceId 资源ID
     */
    void removeRoleResource(Long roleId, Long resourceId);

    /**
     * 批量移除角色的资源权限
     *
     * @param roleId  角色ID
     * @param request 资源ID列表请求
     */
    void batchRemoveRoleResources(Long roleId, ResourceIdsRequest request);

    /**
     * 更新角色的资源权限（全量替换）
     *
     * @param roleId      角色ID
     * @param resourceIds 资源ID列表
     */
    void updateRoleResources(Long roleId, List<Long> resourceIds);

    /**
     * 获取拥有某资源权限的角色列表
     *
     * @param resourceId 资源ID
     * @return 角色选项列表
     */
    List<RoleOptionVO> getResourceRoles(Long resourceId);

    /**
     * 检查角色是否拥有某资源权限
     *
     * @param roleId     角色ID
     * @param resourceId 资源ID
     * @return 是否拥有
     */
    Boolean checkRoleResource(Long roleId, Long resourceId);

    /**
     * 获取角色的资源权限树
     *
     * @param roleId 角色ID
     * @return 资源树列表（带checked状态）
     */
    List<ResourceTreeVO> getRoleResourceTree(Long roleId);

    /**
     * 批量为多个角色分配资源权限
     *
     * @param request 批量分配请求
     * @return 批量操作结果
     */
    BatchResultVO batchAssignResource(ResourceRoleBatchAssignRequest request);

    /**
     * 检查用户是否拥有某资源权限
     *
     * @param userId       用户ID
     * @param resourceCode 资源编码
     * @return 是否拥有及匹配的角色
     */
    Boolean checkUserResourcePermission(Long userId, String resourceCode);
}
