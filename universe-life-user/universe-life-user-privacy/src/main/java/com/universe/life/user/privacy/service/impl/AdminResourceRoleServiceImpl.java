package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.user.privacy.constants.RedisConstants;
import com.universe.life.user.privacy.domain.dto.request.ResourceIdsRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceRoleAssignRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceRoleBatchAssignRequest;
import com.universe.life.user.privacy.domain.po.Resource;
import com.universe.life.user.privacy.domain.po.ResourceRole;
import com.universe.life.user.privacy.domain.po.Role;
import com.universe.life.user.privacy.domain.vo.BatchResultVO;
import com.universe.life.user.privacy.domain.vo.ResourceRoleVO;
import com.universe.life.user.privacy.domain.vo.ResourceTreeVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.enums.ResourceStatus;
import com.universe.life.user.privacy.mapper.AdminResourceRoleMapper;
import com.universe.life.user.privacy.service.IAdminResourceRoleService;
import com.universe.life.user.privacy.service.IAdminResourceService;
import com.universe.life.user.privacy.service.IAdminRoleService;
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
 * 资源角色关联服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminResourceRoleServiceImpl extends ServiceImpl<AdminResourceRoleMapper, ResourceRole>
        implements IAdminResourceRoleService {

    private final IAdminRoleService roleService;
    private final IAdminResourceService resourceService;
    private final AdminResourceRoleMapper resourceRoleMapper;
    private final CacheUtil cacheUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignResources(ResourceRoleAssignRequest request) {
        log.info("为角色分配资源权限，角色ID：{}，资源ID列表：{}", request.getRoleId(), request.getResourceIds());

        Long operatorId = SecurityUtil.getUserId();

        // 检查角色是否存在
        boolean exists = roleService.lambdaQuery()
                .eq(Role::getId, request.getRoleId())
                .exists();
        if (!exists) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.ROLE_NOT_FOUND);
        }
        // 检查资源是否存在
        Long count = resourceService.lambdaQuery()
                .in(Resource::getId, request.getResourceIds())
                .count();
        if (request.getResourceIds().size() != count) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.RESOURCE_NOT_FOUND);
        }
        // 删除角色原有的资源权限
        lambdaUpdate()
                .eq(ResourceRole::getRoleId, request.getRoleId())
                .remove();
        // 添加新的资源权限关联
        List<ResourceRole> resourceRoles = request.getResourceIds().stream()
                .map(resourceId -> new ResourceRole()
                        .setRoleId(request.getRoleId())
                        .setResourceId(resourceId)
                        .setGrantedBy(operatorId))
                .collect(Collectors.toList());

        saveBatch(resourceRoles);
        
        // 批量删除缓存
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(RedisConstants.ROLE_RESOURCES_KEY + request.getRoleId());
        cacheUtil.deleteAll(keysToDelete);

        log.info("为角色分配资源权限成功，角色ID：{}", request.getRoleId());
    }

    @Override
    public List<ResourceRoleVO> getRoleResources(Long roleId) {
        // 1. 尝试从缓存获取
        String cacheKey = RedisConstants.ROLE_RESOURCES_KEY + roleId;
        List<ResourceRoleVO> cached = cacheUtil.getList(cacheKey, ResourceRoleVO.class);
        if (cached != null) {
            return cached;
        }
        
        // 2. 缓存未命中，查询数据库
        List<ResourceRoleVO> resourceRoleVOS = resourceRoleMapper.selectRoleResources(roleId);
        if (CollUtil.isEmpty(resourceRoleVOS)) {
            resourceRoleVOS = new ArrayList<>();
        }
        
        // 3. 写入缓存
        cacheUtil.set(cacheKey, resourceRoleVOS, RedisConstants.getRoleResourcesExpire());
        
        return resourceRoleVOS;
    }

    @Override
    public void removeRoleResource(Long roleId, Long resourceId) {
        log.info("移除角色资源权限，角色ID：{}，资源ID：{}", roleId, resourceId);

        // 先查看是否有角色关联
        boolean removed = lambdaUpdate()
                .eq(ResourceRole::getRoleId, roleId)
                .eq(ResourceRole::getResourceId, resourceId)
                .remove();
        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("删除角色"));
        }
        
        // 批量删除缓存
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(RedisConstants.ROLE_RESOURCES_KEY + roleId);
        cacheUtil.deleteAll(keysToDelete);

        log.info("移除角色资源权限成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveRoleResources(Long roleId, ResourceIdsRequest request) {
        log.info("批量移除角色资源权限，角色ID：{}，资源ID列表：{}", roleId, request.getResourceIds());

        boolean removed = lambdaUpdate()
                .eq(ResourceRole::getRoleId, roleId)
                .in(ResourceRole::getResourceId, request.getResourceIds())
                .remove();
        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("批量删除角色"));
        }
        
        // 批量删除缓存
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(RedisConstants.ROLE_RESOURCES_KEY + roleId);
        cacheUtil.deleteAll(keysToDelete);
        
        log.info("批量移除角色资源权限成功，移除数量：{}", request.getResourceIds().size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleResources(Long roleId, List<Long> resourceIds) {
        log.info("更新角色资源权限，角色ID：{}，资源ID列表：{}", roleId, resourceIds);

        Long operatorId = SecurityUtil.getUserId();

        // 删除角色原有的资源权限
        lambdaUpdate()
                .eq(ResourceRole::getRoleId, roleId)
                .remove();

        // 添加新的资源权限关联
        if (CollUtil.isNotEmpty(resourceIds)) {
            List<ResourceRole> resourceRoles = resourceIds.stream()
                    .map(resourceId -> new ResourceRole()
                            .setRoleId(roleId)
                            .setResourceId(resourceId)
                            .setGrantedBy(operatorId))
                    .collect(Collectors.toList());

            saveBatch(resourceRoles);
        }
        
        // 批量删除缓存
        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(RedisConstants.ROLE_RESOURCES_KEY + roleId);
        cacheUtil.deleteAll(keysToDelete);

        log.info("更新角色资源权限成功");
    }

    @Override
    public List<RoleOptionVO> getResourceRoles(Long resourceId) {
        // 查询拥有该资源权限的角色ID
        List<RoleOptionVO> roleOptionVOS = resourceRoleMapper.selectRoleByResource(resourceId);
        if (CollUtil.isEmpty(roleOptionVOS)) {
            return new ArrayList<>();
        }
        return roleOptionVOS;
    }

    @Override
    public Boolean checkRoleResource(Long roleId, Long resourceId) {
        return lambdaQuery()
                .eq(ResourceRole::getRoleId, roleId)
                .eq(ResourceRole::getResourceId, resourceId)
                .exists();
    }

    @Override
    public List<ResourceTreeVO> getRoleResourceTree(Long roleId) {
        // 查询所有启用的资源
        List<Resource> allResources = resourceService.lambdaQuery()
                .eq(Resource::getStatus, ResourceStatus.ENABLED)
                .orderByAsc(Resource::getSortOrder)
                .list();

        if (CollUtil.isEmpty(allResources)) {
            return new ArrayList<>();
        }

        // 查询角色已有的资源权限
        Set<Long> grantedResourceIds = lambdaQuery()
                .eq(ResourceRole::getRoleId, roleId)
                .list()
                .stream()
                .map(ResourceRole::getResourceId)
                .collect(Collectors.toSet());

        // 构建树形结构
        return buildTree(allResources, 0L, grantedResourceIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultVO batchAssignResource(ResourceRoleBatchAssignRequest request) {
        log.info("批量为角色分配资源权限，角色ID列表：{}，资源ID：{}", request.getRoleIds(), request.getResourceId());

        // 先检验角色是否存在
        Long operatorId = SecurityUtil.getUserId();

        // 检查资源是否存在
        boolean alreadyExists = resourceService.lambdaQuery()
                .eq(Resource::getId, request.getResourceId())
                .eq(Resource::getStatus, ResourceStatus.ENABLED)
                .exists();
        if (!alreadyExists) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.RESOURCE_NOT_FOUND);
        }

        // 先验证角色是否存在
        Long count = roleService.lambdaQuery()
                .in(Role::getId, request.getRoleIds())
                .count();
        if (count != request.getRoleIds().size()) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.PART_OF_ROLE_NOT_FOUND);
        }
        // 更新角色资源权限
        List<ResourceRole> resourceRoles = request.getRoleIds()
                .stream()
                .map(r ->
                        new ResourceRole()
                                .setResourceId(request.getResourceId())
                                .setRoleId(r)
                                .setGrantedBy(operatorId)
                ).toList();
        // 批量保存
        saveBatch(resourceRoles);
        return BatchResultVO.success(request.getRoleIds().size());
    }

    @Override
    public Boolean checkUserResourcePermission(Long userId, String resourceCode) {
        return resourceRoleMapper.checkUserResourcePermission(userId, resourceCode) > 0L;
    }

    /**
     * 构建资源树（带checked状态）
     */
    private List<ResourceTreeVO> buildTree(List<Resource> resources, Long parentId, Set<Long> grantedResourceIds) {
        return resources.stream()
                .filter(r -> {
                    if (parentId == null) {
                        return r.getParentId() == null;
                    }
                    return parentId.equals(r.getParentId());
                })
                .map(r -> {
                    ResourceTreeVO vo = new ResourceTreeVO();
                    vo.setId(r.getId());
                    vo.setResourceCode(r.getResourceCode());
                    vo.setResourceName(r.getResourceName());
                    vo.setResourceType(r.getResourceType());
                    vo.setChecked(grantedResourceIds.contains(r.getId()));
                    vo.setChildren(buildTree(resources, r.getId(), grantedResourceIds));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
