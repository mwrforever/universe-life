package com.universe.life.user.privacy.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.user.privacy.domain.dao.query.ResourceListQuery;
import com.universe.life.user.privacy.domain.dto.request.ResourceCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceUpdateRequest;
import com.universe.life.user.privacy.domain.vo.ResourceDetailVO;
import com.universe.life.user.privacy.domain.vo.ResourceListVO;
import com.universe.life.user.privacy.domain.vo.ResourceTreeVO;
import com.universe.life.user.privacy.enums.ResourceType;
import com.universe.life.user.privacy.service.IAdminResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源管理控制器
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
@Slf4j
@Tag(name = "资源管理", description = "RBAC资源管理相关接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/resource")
@Validated
public class AdminResourceController {

    private final IAdminResourceService resourceService;

    @PostMapping
    @Operation(summary = "创建资源", description = "创建新的资源")
    @PreAuthorize("@pm.match('sys:admin:resource:add')")
    public Result<ResourceDetailVO> createResource(@Valid @RequestBody ResourceCreateRequest request) {
        log.info("创建资源，资源编码：{}", request.getResourceCode());
        return Result.success(resourceService.createResource(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取资源详情", description = "根据ID获取资源详细信息")
    @PreAuthorize("@pm.match('sys:admin:resource:read')")
    public Result<ResourceDetailVO> getResourceById(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id) {
        log.info("查询资源详情，资源ID：{}", id);
        return Result.success(resourceService.getResourceById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新资源", description = "更新资源信息")
    @PreAuthorize("@pm.match('sys:admin:resource:update')")
    public Result<ResourceDetailVO> updateResource(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ResourceUpdateRequest request) {
        log.info("更新资源，资源ID：{}", id);
        return Result.success(resourceService.updateResource(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资源", description = "删除资源")
    @PreAuthorize("@pm.match('sys:admin:resource:delete')")
    public Result<Void> deleteResource(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id) {
        log.info("删除资源，资源ID：{}", id);
        resourceService.deleteResource(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询资源列表", description = "分页查询资源列表")
    @PreAuthorize("@pm.match('sys:admin:resource:list:read')")
    public Result<PageResult<ResourceListVO>> pageResources(@Valid ResourceListQuery query) {
        log.info("分页查询资源列表，查询条件：{}", query);
        return Result.success(resourceService.pageResources(query));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取资源树", description = "获取资源树形结构")
    @PreAuthorize("@pm.match('sys:admin:resource:tree:read')")
    public Result<List<ResourceTreeVO>> getResourceTree(
            @Parameter(description = "微服务名称筛选") @RequestParam(required = false) String serviceName,
            @Parameter(description = "资源类型筛选") @RequestParam(required = false) ResourceType resourceType) {
        log.info("获取资源树，服务名称：{}，资源类型：{}", serviceName, resourceType);
        return Result.success(resourceService.getResourceTree(serviceName, resourceType));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改资源状态", description = "修改资源启用/禁用状态")
    @PreAuthorize("@pm.match('sys:admin:resource:status:update')")
    public Result<Void> updateResourceStatus(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ResourceStatusUpdateRequest request) {
        log.info("修改资源状态，资源ID：{}，状态：{}", id, request.getStatus());
        resourceService.updateResourceStatus(id, request);
        return Result.success();
    }
}
