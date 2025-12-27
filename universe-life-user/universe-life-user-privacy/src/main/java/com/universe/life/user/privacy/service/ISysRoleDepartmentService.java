package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.user.privacy.domain.dto.request.RoleDepartmentAssignRequest;
import com.universe.life.user.privacy.domain.po.SysRoleDepartment;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;

import java.util.List;

/**
 * 部门角色关联服务接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface ISysRoleDepartmentService extends IService<SysRoleDepartment> {

    /**
     * 批量分配部门角色
     *
     * @param request 分配请求
     */
    void assignRoleDepartments(RoleDepartmentAssignRequest request);

    /**
     * 批量移除部门角色
     *
     * @param request 移除请求
     */
    void removeRoleDepartments(RoleDepartmentAssignRequest request);

    /**
     * 获取角色关联的部门列表
     *
     * @param roleId 角色ID
     * @return 部门列表
     */
    List<SysDepartmentSimpleVO> getRoleDepartments(Long roleId);

    /**
     * 获取部门关联的角色列表
     *
     * @param departmentId 部门ID
     * @return 角色列表
     */
    List<RoleOptionVO> getDepartmentRoles(Long departmentId);
}
