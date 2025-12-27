package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.common.result.PageResult;
import com.universe.life.user.privacy.domain.dao.query.ResourceListQuery;
import com.universe.life.user.privacy.domain.dto.request.ResourceCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceUpdateRequest;
import com.universe.life.user.privacy.domain.po.Resource;
import com.universe.life.user.privacy.domain.vo.ResourceDetailVO;
import com.universe.life.user.privacy.domain.vo.ResourceListVO;
import com.universe.life.user.privacy.domain.vo.ResourceTreeVO;
import com.universe.life.user.privacy.enums.ResourceStatus;
import com.universe.life.user.privacy.enums.ResourceType;
import com.universe.life.user.privacy.mapper.AdminResourceMapper;
import com.universe.life.user.privacy.mapstruct.ResourceMapstruct;
import com.universe.life.user.privacy.service.IAdminResourceService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceDetailVO createResource(ResourceCreateRequest request) {
        log.info("创建资源，资源编码：{}", request.getResourceCode());
        // 如果有父资源，检查父资源是否存在
        if (ObjectUtil.isNotNull(request.getParentId())) {
            boolean exists = lambdaQuery()
                    .eq(Resource::getId, request.getParentId())
                    .exists();
            if (!exists) {
                throw new BusinessException.DataNotFoundException(ExceptionMessage.PARENT_RESOURCE_NOT_FOUND);
            }
        }

        // 创建资源
        Resource resource = resourceMapstruct.toPo(request);
        boolean saved = save(resource);
        if (!saved) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.OPERATION_FAILED);
        }
        log.info("创建资源成功，资源ID：{}", resource.getId());
        return resourceMapstruct.toDetailVO(resource);
    }

    @Override
    public ResourceDetailVO getResourceById(Long id) {
        Resource resource = getById(id);
        if (ObjectUtil.isNull(resource)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        return resourceMapstruct.toDetailVO(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceDetailVO updateResource(Long id, ResourceUpdateRequest request) {
        log.info("更新资源，资源ID：{}", id);
        // 如果有父资源，检查父资源是否存在且不能是自己
        if (ObjectUtil.isNotNull(request.getParentId())) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException.OperationNotAllowedException(ExceptionMessage.PARENT_RESOURCE_NOT_BE_SELF);
            }
            boolean exists = lambdaQuery()
                    .eq(Resource::getId, request.getParentId())
                    .exists();
            if (!exists) {
                throw new BusinessException.DataNotFoundException(ExceptionMessage.PARENT_RESOURCE_NOT_FOUND);
            }
        }

        // 更新资源
        Resource resource = resourceMapstruct.toPo(request);
        resource.setId(id);
        updateById(resource);

        log.info("更新资源成功，资源ID：{}", id);
        return resourceMapstruct.toDetailVO(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Long id) {
        log.info("删除资源，资源ID：{}", id);
        // 检查是否有子资源
        boolean hasChildren = lambdaQuery()
                .eq(Resource::getParentId, id)
                .exists();
        if (hasChildren) {
            throw new BusinessException.OperationNotAllowedException("不能删除有子资源的资源");
        }
        removeById(id);
        log.info("删除资源成功，资源ID：{}", id);
    }

    @Override
    public PageResult<ResourceListVO> pageResources(ResourceListQuery query) {
        IPage<Resource> page = new Page<>(query.getPage(), query.getSize());

        IPage<Resource> result = lambdaQuery()
                .eq(ObjectUtil.isNotNull(query.getResourceType()), Resource::getResourceType, query.getResourceType())
                .eq(StrUtil.isNotBlank(query.getServiceName()), Resource::getServiceName, query.getServiceName())
                .eq(ObjectUtil.isNotNull(query.getStatus()), Resource::getStatus, query.getStatus())
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
        // 查询所有资源
        List<Resource> resources = lambdaQuery()
                .eq(StrUtil.isNotBlank(serviceName), Resource::getServiceName, serviceName)
                .eq(ObjectUtil.isNotNull(resourceType), Resource::getResourceType, resourceType)
                .eq(Resource::getStatus, ResourceStatus.ENABLED)
                .orderByAsc(Resource::getSortOrder)
                .list();

        if (CollUtil.isEmpty(resources)) {
            return new ArrayList<>();
        }

        // 构建树形结构
        return buildTree(resources, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResourceStatus(Long id, ResourceStatusUpdateRequest request) {
        log.info("更新资源状态，资源ID：{}，状态：{}", id, request.getStatus());

        Resource resource = lambdaQuery()
                .select(Resource::getStatus)
                .eq(Resource::getId, id)
                .one();
        if (ObjectUtil.isNull(resource)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 转换状态
        ResourceStatus status = request.getStatus().isEnable() ? ResourceStatus.ENABLED
                : ResourceStatus.DISABLED;

        lambdaUpdate()
                .set(Resource::getStatus, status)
                .eq(Resource::getId, id)
                .update();

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
