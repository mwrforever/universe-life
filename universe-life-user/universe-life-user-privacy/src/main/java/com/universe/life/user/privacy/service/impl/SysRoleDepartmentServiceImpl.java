package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.user.privacy.domain.dto.request.RoleDepartmentAssignRequest;
import com.universe.life.user.privacy.domain.po.Role;
import com.universe.life.user.privacy.domain.po.SysRoleDepartment;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.mapper.SysRoleDepartmentMapper;
import com.universe.life.user.privacy.mapstruct.RoleMapstruct;
import com.universe.life.user.privacy.service.IAdminRoleService;
import com.universe.life.user.privacy.service.ISysRoleDepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门角色关联服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleDepartmentServiceImpl extends ServiceImpl<SysRoleDepartmentMapper, SysRoleDepartment> implements ISysRoleDepartmentService {

    private final IAdminRoleService roleService;

    private final RoleMapstruct roleMapstruct;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleDepartments(RoleDepartmentAssignRequest request) {
        log.info("分配部门角色，角色ID：{}，部门ID列表：{}", request.getRoleId(), request.getDepartmentIds());

        List<SysRoleDepartment> roleDepartments = new ArrayList<>();
        for (Long departmentId : request.getDepartmentIds()) {
            // 检查是否已存在关联
            boolean exists = lambdaQuery()
                    .eq(SysRoleDepartment::getRoleId, request.getRoleId())
                    .eq(SysRoleDepartment::getDepartmentId, departmentId)
                    .exists();

            if (!exists) {
                SysRoleDepartment roleDepartment = new SysRoleDepartment();
                roleDepartment.setRoleId(request.getRoleId());
                roleDepartment.setDepartmentId(departmentId);
                roleDepartments.add(roleDepartment);
            }
        }

        if (CollUtil.isNotEmpty(roleDepartments)) {
            saveBatch(roleDepartments);
        }

        log.info("分配部门角色成功，角色ID：{}", request.getRoleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleDepartments(RoleDepartmentAssignRequest request) {
        log.info("移除部门角色，角色ID：{}，部门ID列表：{}", request.getRoleId(), request.getDepartmentIds());

        remove(new LambdaQueryWrapper<SysRoleDepartment>()
                .eq(SysRoleDepartment::getRoleId, request.getRoleId())
                .in(SysRoleDepartment::getDepartmentId, request.getDepartmentIds()));

        log.info("移除部门角色成功，角色ID：{}", request.getRoleId());
    }

    @Override
    public List<SysDepartmentSimpleVO> getRoleDepartments(Long roleId) {
        return baseMapper.selectDepartmentsByRoleId(roleId);
    }

    @Override
    public List<RoleOptionVO> getDepartmentRoles(Long departmentId) {
        List<Long> roleIds = baseMapper.selectRoleIdsByDepartmentId(departmentId);

        if (CollUtil.isEmpty(roleIds)) {
            return new ArrayList<>();
        }

        List<Role> roles = roleService.lambdaQuery()
                .select(Role::getId, Role::getRoleCode, Role::getRoleName)
                .in(Role::getId, roleIds)
                .list();

        if (CollUtil.isEmpty(roles)) {
            return new ArrayList<>();
        }

        return roleMapstruct.toOptionVOList(roles);
    }
}
