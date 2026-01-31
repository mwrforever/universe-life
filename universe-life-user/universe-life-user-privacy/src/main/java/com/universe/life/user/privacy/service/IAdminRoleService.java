package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.common.domain.PageResult;
import com.universe.life.user.privacy.domain.dao.query.RoleListQuery;
import com.universe.life.user.privacy.domain.dto.request.RoleCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.RoleStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.RoleUpdateRequest;
import com.universe.life.user.privacy.domain.po.Role;
import com.universe.life.user.privacy.domain.vo.ResourceSimpleVO;
import com.universe.life.user.privacy.domain.vo.RoleDetailVO;
import com.universe.life.user.privacy.domain.vo.RoleListVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;

import java.util.List;

/**
 * 角色管理服务接口
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
public interface IAdminRoleService extends IService<Role> {

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @return 角色详情
     */
    void createRole(RoleCreateRequest request);

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    RoleDetailVO getRoleById(Long id);

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param request 更新请求
     */
    void updateRole(Long id, RoleUpdateRequest request);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 分页查询角色列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<RoleListVO> pageRoles(RoleListQuery query);

    /**
     * 更新角色状态
     *
     * @param id      角色ID
     * @param request 状态更新请求
     */
    void updateRoleStatus(Long id, RoleStatusUpdateRequest request);

    /**
     * 获取角色的资源权限列表
     *
     * @param roleId 角色ID
     * @return 资源列表
     */
    List<ResourceSimpleVO> getRoleResources(Long roleId);

    /**
     * 获取所有启用的角色选项
     *
     * @return 角色选项列表
     */
    List<RoleOptionVO> getRoleOptions();
}
