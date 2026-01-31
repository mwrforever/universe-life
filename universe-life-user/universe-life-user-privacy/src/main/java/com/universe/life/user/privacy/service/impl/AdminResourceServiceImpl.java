package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.common.domain.PageResult;
import com.universe.life.user.privacy.constants.RedisConstants;
import com.universe.life.user.privacy.domain.dao.query.ResourceListQuery;
import com.universe.life.user.privacy.domain.dto.request.ResourceCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceUpdateRequest;
import com.universe.life.user.privacy.domain.po.Resource;
import com.universe.life.user.privacy.domain.vo.ResourceDetailVO;
import com.universe.life.user.privacy.domain.vo.ResourceListVO;
import com.universe.life.user.privacy.domain.vo.ResourceTreeVO;
import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.ResourceStatus;
import com.universe.life.user.privacy.enums.ResourceType;
import com.universe.life.user.privacy.mapper.AdminResourceMapper;
import com.universe.life.user.privacy.mapstruct.ResourceMapstruct;
import com.universe.life.user.privacy.service.IAdminResourceService;
import com.universe.life.common.util.CacheUtil;
import com.universe.life.user.privacy.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源管理服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminResourceServiceImpl extends ServiceImpl<AdminResourceMapper, Resource>
        implements IAdminResourceService {

    private final ResourceMapstruct resourceMapstruct;
    private final CacheUtil cacheUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createResource(ResourceCreateRequest request) {
        log.info("创建资源，资源编码：{}", request.getResourceCode());

        validateResourceCreation(request);

        Resource resource = resourceMapstruct.toPo(request);
        boolean saved = save(resource);
        if (!saved) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.OPERATION_FAILED);
        }

        invalidateResourceCaches(null);

        log.info("创建资源成功，资源ID：{}", resource.getId());
    }

    private void validateResourceCreation(ResourceCreateRequest request) {
        if (request.getParentId() == 0) {
            throw new BusinessException.OperationFailedException(
                    ExceptionMessage.Formatter.operationFailed("请关联正确的父资源")
            );
        }

        boolean existsResourceCode = lambdaQuery()
                .eq(Resource::getResourceCode, request.getResourceCode())
                .exists();
        ValidationHelper.validateNotExists(existsResourceCode, "资源编码");

        if (ObjectUtil.isNotNull(request.getParentId())) {
            boolean parentExists = lambdaQuery()
                    .eq(Resource::getId, request.getParentId())
                    .exists();
            ValidationHelper.validateExists(parentExists, "父资源");
        }
    }

    private void invalidateResourceCaches(Long resourceId) {
        List<String> keys = new ArrayList<>(RedisConstants.getAllResourceTreeKeys());
        if (resourceId != null) {
            keys.add(RedisConstants.buildResourceKey(resourceId, "info"));
        }
        cacheUtil.deleteAll(keys);
    }

    @Override
    public ResourceDetailVO getResourceById(Long id) {
        String cacheKey = RedisConstants.RESOURCE_INFO_KEY + id;
        return cacheUtil.getOrCompute(
                cacheKey,
                ResourceDetailVO.class,
                () -> fetchAndConvertResource(id),
                RedisConstants.getResourceInfoExpire()
        );
    }

    private ResourceDetailVO fetchAndConvertResource(Long id) {
        Resource resource = getById(id);
        ValidationHelper.validateNotNull(resource, "资源");
        return resourceMapstruct.toDetailVO(resource);
    }

    @Override
    public void updateResource(Long id, ResourceUpdateRequest request) {
        log.info("更新资源，资源ID：{}", id);

        validateResourceUpdate(id, request);

        Resource resource = resourceMapstruct.toPo(request);
        resource.setId(id);
        updateById(resource);

        invalidateResourceCaches(id);

        log.info("更新资源成功，资源ID：{}", id);
    }

    private void validateResourceUpdate(Long id, ResourceUpdateRequest request) {
        if (request.getParentId() == 0) {
            throw new BusinessException.OperationFailedException(
                    ExceptionMessage.Formatter.operationFailed("请关联正确的父资源")
            );
        }

        if (ObjectUtil.isNotNull(request.getParentId())) {
            ValidationHelper.validateParentNotSelf(request.getParentId(), id, "资源");

            boolean parentExists = lambdaQuery()
                    .eq(Resource::getId, request.getParentId())
                    .exists();
            ValidationHelper.validateExists(parentExists, "父资源");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Long id) {
        log.info("删除资源，资源ID：{}", id);

        boolean hasChildren = lambdaQuery()
                .eq(Resource::getParentId, id)
                .exists();
        ValidationHelper.validateNoChildren(hasChildren, "资源");

        removeById(id);
        invalidateResourceCaches(id);

        log.info("删除资源成功，资源ID：{}", id);
    }

    @Override
    public PageResult<ResourceListVO> pageResources(ResourceListQuery query) {
        IPage<Resource> page = new Page<>(query.getPage(), query.getSize());

        // 将 Integer 转换为枚举
        ResourceType resourceType = ResourceType.of(query.getResourceType());
        CommonStatus commonStatus = CommonStatus.of(query.getStatus());

        IPage<Resource> result = lambdaQuery()
                .eq(ObjectUtil.isNotNull(resourceType), Resource::getResourceType, resourceType)
                .eq(StrUtil.isNotBlank(query.getServiceName()), Resource::getServiceName, query.getServiceName())
                .eq(ObjectUtil.isNotNull(commonStatus), Resource::getStatus, commonStatus)
                .and(StrUtil.isNotBlank(query.getKeyword()),
                        wrapper -> wrapper.like(Resource::getResourceCode, query.getKeyword())
                                .or()
                                .like(Resource::getResourceName, query.getKeyword()))
                .orderByAsc(Resource::getSortOrder)
                .orderByDesc(Resource::getCreatedAt)
                .page(page);

        List<Resource> records = result.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageResult.empty(page);
        }

        List<ResourceListVO> list = resourceMapstruct.toListVO(records);

        return PageResult.of(list, page);
    }

    @Override
    public List<ResourceTreeVO> getResourceTree(String serviceName, ResourceType resourceType) {
        Integer typeValue = resourceType == null ? null : resourceType.getCode();
        String cacheKey = RedisConstants.buildResourceTreeKey(serviceName, typeValue);

        return cacheUtil.getListOrCompute(
                cacheKey,
                ResourceTreeVO.class,
                () -> fetchAndBuildResourceTree(serviceName, resourceType),
                RedisConstants.getResourceTreeExpire()
        );
    }

    private List<ResourceTreeVO> fetchAndBuildResourceTree(String serviceName, ResourceType resourceType) {
        List<Resource> resources = lambdaQuery()
                .eq(StrUtil.isNotBlank(serviceName), Resource::getServiceName, serviceName)
                .eq(ObjectUtil.isNotNull(resourceType), Resource::getResourceType, resourceType)
                .eq(Resource::getStatus, ResourceStatus.ENABLED)
                .orderByAsc(Resource::getSortOrder)
                .list();

        if (CollUtil.isEmpty(resources)) {
            return new ArrayList<>();
        }

        return buildTree(resources, 0L);
    }

    @Override
    @Transactional
    public void updateResourceStatus(Long id, ResourceStatusUpdateRequest request) {
        log.info("更新资源状态，资源ID：{}，状态：{}", id, request.getStatus());

        Resource resource = lambdaQuery()
                .select(Resource::getStatus)
                .eq(Resource::getId, id)
                .one();
        ValidationHelper.validateNotNull(resource, "资源");

        ResourceStatus status = request.getStatus().isEnable() ? ResourceStatus.ENABLED : ResourceStatus.DISABLED;

        lambdaUpdate()
                .set(Resource::getStatus, status)
                .eq(Resource::getId, id)
                .update();

        invalidateResourceCaches(id);

        log.info("更新资源状态成功，资源ID：{}", id);
    }

    /**
     * 构建资源树
     */
    private List<ResourceTreeVO> buildTree(List<Resource> resources, Long parentId) {
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
                    vo.setChecked(false);
                    vo.setChildren(buildTree(resources, r.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
