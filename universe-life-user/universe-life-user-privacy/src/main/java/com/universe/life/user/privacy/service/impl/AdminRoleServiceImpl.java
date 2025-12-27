package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.common.result.PageResult;
import com.universe.life.user.privacy.domain.dao.query.RoleListQuery;
import com.universe.life.user.privacy.domain.dto.request.RoleCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.RoleStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.RoleUpdateRequest;
import com.universe.life.user.privacy.domain.po.Resource;
import com.universe.life.user.privacy.domain.po.ResourceRole;
import com.universe.life.user.privacy.domain.po.Role;
import com.universe.life.user.privacy.domain.po.UserRole;
import com.universe.life.user.privacy.domain.vo.ResourceSimpleVO;
import com.universe.life.user.privacy.domain.vo.RoleDetailVO;
import com.universe.life.user.privacy.domain.vo.RoleListVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.RoleType;
import com.universe.life.user.privacy.mapper.AdminResourceRoleMapper;
import com.universe.life.user.privacy.mapper.AdminRoleMapper;
import com.universe.life.user.privacy.mapstruct.RoleMapstruct;
import com.universe.life.user.privacy.service.IAdminResourceService;
import com.universe.life.user.privacy.service.IAdminRoleService;
import com.universe.life.user.privacy.service.IAdminUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, Role> implements IAdminRoleService {

    private final AdminResourceRoleMapper resourceRoleMapper;

    private final IAdminResourceService resourceService;

    private final IAdminUserRoleService userRoleService;

    private final RoleMapstruct roleMapstruct;

    @Override
    public RoleDetailVO createRole(RoleCreateRequest request) {
        log.info("创建角色，角色编码：{}", request.getRoleCode());
        // 创建角色
        Role role = roleMapstruct.toPo(request);
        boolean saved = save(role);
        if (!saved) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("角色新增"));
        }
        log.info("创建角色成功，角色ID：{}", role.getId());
        return roleMapstruct.toDetailVO(role);
    }

    @Override
    public RoleDetailVO getRoleById(Long id) {
        Role role = getById(id);
        if (ObjectUtil.isNull(role)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        return roleMapstruct.toDetailVO(role);
    }

    @Override
    public RoleDetailVO updateRole(Long id, RoleUpdateRequest request) {
        log.info("更新角色，角色ID：{}", id);
        // 更新角色
        Role role = roleMapstruct.toPo(request);
        boolean updated = updateById(role);
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("角色更新"));
        }
        log.info("更新角色成功，角色ID：{}", id);
        return roleMapstruct.toDetailVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        log.info("删除角色，角色ID：{}", id);

        // 检查角色是否是系统角色
        boolean exists = lambdaQuery()
                .eq(Role::getId, id)
                .eq(Role::getRoleType, RoleType.SYSTEM)
                .exists();
        if (exists) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.Formatter.operationFailed("系统角色不允许删除"));
        }
        // 检查是否有用户关联该角色
        Long count = userRoleService.lambdaQuery()
                .eq(UserRole::getRoleId, id)
                .count();

        if (count > 0) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.Formatter.operationFailed("角色已关联用户，不允许删除"));
        }
        // 删除角色关联的资源权限
        resourceRoleMapper.delete(new LambdaQueryWrapper<ResourceRole>().eq(ResourceRole::getRoleId, id));
        removeById(id);
        log.info("删除角色成功，角色ID：{}", id);
    }

    @Override
    public PageResult<RoleListVO> pageRoles(RoleListQuery query) {
        IPage<Role> page = new Page<>(query.getPage(), query.getSize());

        IPage<Role> result = lambdaQuery()
                .eq(ObjectUtil.isNotNull(query.getRoleType()), Role::getRoleType, query.getRoleType())
                .eq(ObjectUtil.isNotNull(query.getStatus()), Role::getStatus, query.getStatus())
                .and(StrUtil.isNotBlank(query.getKeyword()),
                        wrapper -> wrapper.like(Role::getRoleCode, query.getKeyword())
                                .or()
                                .like(Role::getRoleName, query.getKeyword()))
                .orderByAsc(Role::getSortOrder)
                .orderByDesc(Role::getCreatedAt)
                .page(page);

        List<Role> records = result.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageResult.empty(page);
        }

        // 转换为VO
        List<RoleListVO> list = roleMapstruct.toListVO(records);

        return PageResult.of(list, page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleStatus(Long id, RoleStatusUpdateRequest request) {
        log.info("更新角色状态，角色ID：{}，状态：{}", id, request.getStatus());

        boolean updated = lambdaUpdate()
                .set(Role::getStatus, request.getStatus())
                .eq(Role::getId, id)
                .update();

        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("角色状态更新"));
        }

        log.info("更新角色状态成功，角色ID：{}", id);
    }

    @Override
    public List<ResourceSimpleVO> getRoleResources(Long roleId) {
        // 查询角色关联的资源ID
        List<ResourceRole> resourceRoles = resourceRoleMapper.selectList(
                new LambdaQueryWrapper<ResourceRole>()
                        .eq(ResourceRole::getRoleId, roleId));

        if (CollUtil.isEmpty(resourceRoles)) {
            return new ArrayList<>();
        }

        Set<Long> resourceIds = resourceRoles.stream()
                .map(ResourceRole::getResourceId)
                .collect(Collectors.toSet());

        // 查询资源信息
        List<Resource> resources = resourceService.lambdaQuery()
                .select(
                        Resource::getId,
                        Resource::getResourceCode,
                        Resource::getResourceName,
                        Resource::getResourceType
                ).in(Resource::getId, resourceIds)
                .list();

        return roleMapstruct.toSimpleVO(resources);
    }

    @Override
    public List<RoleOptionVO> getRoleOptions() {
        List<Role> roles = lambdaQuery()
                .select(Role::getId, Role::getRoleCode, Role::getRoleName)
                .eq(Role::getStatus, CommonStatus.ENABLE)
                .orderByAsc(Role::getSortOrder)
                .list();

        if (CollUtil.isEmpty(roles)) {
            return new ArrayList<>();
        }

        return roleMapstruct.toOptionVOList(roles);
    }
}
