package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.common.result.PageResult;
import com.universe.life.user.privacy.domain.dao.query.ResourceListQuery;
import com.universe.life.user.privacy.domain.dto.request.ResourceCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceUpdateRequest;
import com.universe.life.user.privacy.domain.po.Resource;
import com.universe.life.user.privacy.domain.vo.ResourceDetailVO;
import com.universe.life.user.privacy.domain.vo.ResourceListVO;
import com.universe.life.user.privacy.domain.vo.ResourceTreeVO;
import com.universe.life.user.privacy.enums.ResourceType;

import java.util.List;

/**
 * 资源管理服务接口
 *
 * @author 毛伟然
 * @since 2025-12-08
 */
public interface IAdminResourceService extends IService<Resource> {

    /**
     * 创建资源
     *
     * @param request 创建请求
     * @return 资源详情
     */
    ResourceDetailVO createResource(ResourceCreateRequest request);

    /**
     * 获取资源详情
     *
     * @param id 资源ID
     * @return 资源详情
     */
    ResourceDetailVO getResourceById(Long id);

    /**
     * 更新资源
     *
     * @param id      资源ID
     * @param request 更新请求
     * @return 资源详情
     */
    ResourceDetailVO updateResource(Long id, ResourceUpdateRequest request);

    /**
     * 删除资源
     *
     * @param id 资源ID
     */
    void deleteResource(Long id);

    /**
     * 分页查询资源列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<ResourceListVO> pageResources(ResourceListQuery query);

    /**
     * 获取资源树
     *
     * @param serviceName  服务名称筛选
     * @param resourceType 资源类型筛选
     * @return 资源树列表
     */
    List<ResourceTreeVO> getResourceTree(String serviceName, ResourceType resourceType);

    /**
     * 更新资源状态
     *
     * @param id      资源ID
     * @param request 状态更新请求
     */
    void updateResourceStatus(Long id, ResourceStatusUpdateRequest request);
}
