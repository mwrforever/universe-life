package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.user.privacy.domain.dto.request.RoleIdsRequest;
import com.universe.life.user.privacy.domain.dto.request.UserRoleAssignRequest;
import com.universe.life.user.privacy.domain.dto.request.UserRoleBatchAssignRequest;
import com.universe.life.user.privacy.domain.po.Role;
import com.universe.life.user.privacy.domain.po.UserRole;
import com.universe.life.user.privacy.domain.vo.*;
import com.universe.life.user.privacy.mapper.AdminResourceRoleMapper;
import com.universe.life.user.privacy.mapper.AdminRoleMapper;
import com.universe.life.user.privacy.mapper.AdminUserRoleMapper;
import com.universe.life.user.privacy.service.IAdminUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户-角色关联服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserRoleServiceImpl extends ServiceImpl<AdminUserRoleMapper, UserRole>
        implements IAdminUserRoleService {

    private final AdminRoleMapper roleMapper;
    private final AdminResourceRoleMapper resourceRoleMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserRoleDetailVO> assignRoles(UserRoleAssignRequest request) {
        log.info("为用户分配角色，用户ID：{}，角色ID列表：{}", request.getUserId(), request.getRoleIds());

        Long operatorId = SecurityUtil.getUserId();

        // 检查角色是否存在
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<Role>().in(Role::getId, request.getRoleIds()));
        if (count != request.getRoleIds().size()) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.PART_OF_ROLE_NOT_FOUND);
        }
        // 删除用户原有的角色
        lambdaUpdate()
                .eq(UserRole::getUserId, request.getUserId())
                .remove();
        // 添加新的角色关联
        List<UserRole> userRoles = request.getRoleIds().stream()
                .map(roleId -> new UserRole()
                        .setUserId(request.getUserId())
                        .setRoleId(roleId)
                        .setGrantedBy(operatorId))
                .collect(Collectors.toList());

        saveBatch(userRoles);

        log.info("为用户分配角色成功，用户ID：{}", request.getUserId());
        return getUserRoles(request.getUserId());
    }

    @Override
    public List<UserRoleDetailVO> getUserRoles(Long userId) {
        // 从数据库中查询用户角色
        List<UserRoleDetailVO> userRoles = adminUserRoleMapper.getUserRolesDetail(userId);
        if (CollUtil.isEmpty(userRoles)) {
            return Collections.emptyList();
        }
        return userRoles;
    }

    @Override
    public void removeUserRole(Long userId, Long roleId) {
        log.info("移除用户角色，用户ID：{}，角色ID：{}", userId, roleId);

        boolean update = lambdaUpdate()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId)
                .update();

        if (!update) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户与角色关系删除"));
        }

        log.info("移除用户角色成功");
    }

    @Override
    public Integer batchRemoveUserRoles(Long userId, RoleIdsRequest request) {
        log.info("批量移除用户角色，用户ID：{}，角色ID列表：{}", userId, request.getRoleIds());

        boolean removed = lambdaUpdate()
                .in(UserRole::getRoleId, request.getRoleIds())
                .eq(UserRole::getUserId, userId)
                .remove();
        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户与角色关系批量删除"));
        }
        log.info("批量移除用户角色成功，移除数量：{}", request.getRoleIds().size());
        return request.getRoleIds().size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        log.info("更新用户角色，用户ID：{}，角色ID列表：{}", userId, roleIds);

        Long operatorId = SecurityUtil.getUserId();

        // 删除用户原有的角色
        remove(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));

        // 添加新的角色关联
        if (CollUtil.isNotEmpty(roleIds)) {
            List<UserRole> userRoles = roleIds.stream()
                    .map(roleId -> new UserRole()
                            .setUserId(userId)
                            .setRoleId(roleId)
                            .setGrantedBy(operatorId))
                    .collect(Collectors.toList());

            saveBatch(userRoles);
        }

        log.info("更新用户角色成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultVO batchAssignRole(UserRoleBatchAssignRequest request) {
        log.info("批量为用户分配角色，用户ID列表：{}，角色ID：{}", request.getUserIds(), request.getRoleId());

        Long operatorId = SecurityUtil.getUserId();

        // 检查角色是否存在
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<Role>().eq(Role::getId, request.getRoleId()));
        if (count != 1) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.ROLE_NOT_FOUND);
        }

        // 添加新的角色关联
        List<UserRole> userRoles = request.getUserIds().stream()
                .map(userId -> new UserRole().setUserId(userId).setRoleId(request.getRoleId()).setGrantedBy(operatorId))
                .toList();
        saveBatch(userRoles);
        return BatchResultVO.success(request.getUserIds().size());
    }

    @Override
    public Boolean checkUserRole(Long userId, Long roleId) {
        return lambdaQuery()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId)
                .exists();
    }

    @Override
    @Transactional
    public UserPermissionVO getUserPermissions(Long userId) {
        // 获取用户关联的角色信息角色的信息
        List<RoleOptionVO> roleOptionVOList = adminUserRoleMapper.getRoleOptionVOList(userId);
        // 收集角色 id
        Set<Long> roleIds = roleOptionVOList.stream().map(RoleOptionVO::getId).collect(Collectors.toSet());
        UserPermissionVO vo = new UserPermissionVO();
        if (CollUtil.isEmpty(roleIds)) {
            return vo;
        }
        vo.setUserId(userId);
        vo.setRoles(roleOptionVOList);
        // 获取用户的资源列表
        List<ResourceSimpleVO> resourceSimpleVOList = resourceRoleMapper.selectResourceSimpleVOList(roleIds);
        if (CollUtil.isNotEmpty(resourceSimpleVOList)) {
            vo.setPermissions(resourceSimpleVOList);
        }
        return vo;
    }
}
