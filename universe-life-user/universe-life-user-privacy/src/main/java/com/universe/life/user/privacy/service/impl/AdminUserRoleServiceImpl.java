package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.user.privacy.constants.RedisConstants;
import com.universe.life.user.privacy.domain.dto.request.RoleIdsRequest;
import com.universe.life.user.privacy.domain.dto.request.UserRoleAssignRequest;
import com.universe.life.user.privacy.domain.po.Role;
import com.universe.life.user.privacy.domain.po.UserRole;
import com.universe.life.user.privacy.domain.vo.AssignedRoleVO;
import com.universe.life.user.privacy.domain.vo.ResourceSimpleVO;
import com.universe.life.user.privacy.domain.vo.RoleAssignmentStatusVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.domain.vo.UnassignedRoleVO;
import com.universe.life.user.privacy.domain.vo.UserPermissionVO;
import com.universe.life.user.privacy.mapper.AdminResourceRoleMapper;
import com.universe.life.user.privacy.mapper.AdminRoleMapper;
import com.universe.life.user.privacy.mapper.AdminUserRoleMapper;
import com.universe.life.user.privacy.mapper.SysRoleDepartmentMapper;
import com.universe.life.user.privacy.service.IAdminUserRoleService;
import com.universe.life.common.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final SysRoleDepartmentMapper sysRoleDepartmentMapper;
    private final CacheUtil cacheUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(UserRoleAssignRequest request) {
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

        // 批量删除缓存
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(RedisConstants.USER_ROLES_KEY + request.getUserId());
        keysToDelete.add(RedisConstants.USER_ROLE_STATUS_KEY + request.getUserId());
        cacheUtil.deleteAll(keysToDelete);

        log.info("为用户分配角色成功，用户ID：{}", request.getUserId());
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

        // 批量删除缓存
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(RedisConstants.USER_ROLES_KEY + userId);
        keysToDelete.add(RedisConstants.USER_ROLE_STATUS_KEY + userId);
        cacheUtil.deleteAll(keysToDelete);

        log.info("移除用户角色成功");
    }

    @Override
    public void batchRemoveUserRoles(Long userId, RoleIdsRequest request) {
        log.info("批量移除用户角色，用户ID：{}，角色ID列表：{}", userId, request.getRoleIds());

        boolean removed = lambdaUpdate()
                .in(UserRole::getRoleId, request.getRoleIds())
                .eq(UserRole::getUserId, userId)
                .remove();
        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户与角色关系批量删除"));
        }

        // 批量删除缓存
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(RedisConstants.USER_ROLES_KEY + userId);
        keysToDelete.add(RedisConstants.USER_ROLE_STATUS_KEY + userId);
        cacheUtil.deleteAll(keysToDelete);

        log.info("批量移除用户角色成功，移除数量：{}", request.getRoleIds().size());
    }

    @Override
    @Transactional
    public UserPermissionVO getUserPermissions(Long userId) {
        String key = RedisConstants.USER_ROLES_KEY + userId;
        UserPermissionVO userPermissionVO = cacheUtil.get(key, UserPermissionVO.class);
        if (ObjectUtil.isNotNull(userPermissionVO)) {
            return userPermissionVO;
        }
        // 获取用户关联的角色信息角色的信息
        List<RoleOptionVO> roleOptionVOList = getAllRoleList(userId);
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
        cacheUtil.set(key, vo, RedisConstants.getUserRolesExpire());
        return vo;
    }

    @Override
    public RoleAssignmentStatusVO getRoleAssignmentStatus(Long userId) {
        // 1. 尝试从缓存获取
        String cacheKey = RedisConstants.USER_ROLE_STATUS_KEY + userId;
        RoleAssignmentStatusVO cached = cacheUtil.get(cacheKey, RoleAssignmentStatusVO.class);
        if (cached != null) {
            log.debug("缓存命中: {}", cacheKey);
            return cached;
        }

        // 2. 缓存未命中，查询数据库
        log.debug("缓存未命中: {}", cacheKey);

        // 查询所有启用的角色
        List<Role> allRoles = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 1)
                .eq(Role::getDeleted, false)
                .orderByAsc(Role::getSortOrder));

        // 查询用户已分配的角色
        List<AssignedRoleVO> assignedRoles = adminUserRoleMapper.selectAssignedRolesByUserId(userId);

        // 获取已分配角色的ID集合
        Set<Long> assignedRoleIds = assignedRoles.stream()
                .map(AssignedRoleVO::getId)
                .collect(Collectors.toSet());

        // 计算未分配的角色
        List<UnassignedRoleVO> unassignedRoles = allRoles.stream()
                .filter(role -> !assignedRoleIds.contains(role.getId()))
                .map(role -> {
                    UnassignedRoleVO vo = new UnassignedRoleVO();
                    vo.setId(role.getId());
                    vo.setRoleCode(role.getRoleCode());
                    vo.setRoleName(role.getRoleName());
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建结果
        RoleAssignmentStatusVO result = new RoleAssignmentStatusVO();
        result.setUserId(userId);
        result.setAssignedRoles(assignedRoles);
        result.setUnassignedRoles(unassignedRoles);

        // 3. 写入缓存
        cacheUtil.set(cacheKey, result, RedisConstants.getUserRoleStatusExpire());

        return result;
    }

    private List<RoleOptionVO> getAllRoleList(Long userId) {

        // 查询用户角色关系表
        List<RoleOptionVO> userRoleList = adminUserRoleMapper.getRoleOptionVOList(userId);
        // 查询部门角色表
        List<RoleOptionVO> departmentRoleList = sysRoleDepartmentMapper.selectRoleOptionVoList(userId);

        List<RoleOptionVO> roleOptionVOList = new ArrayList<>(userRoleList);
        roleOptionVOList.addAll(departmentRoleList);

        return roleOptionVOList;
    }
}
