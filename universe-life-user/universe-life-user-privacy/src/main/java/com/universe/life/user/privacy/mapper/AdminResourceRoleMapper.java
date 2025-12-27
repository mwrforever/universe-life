package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.user.privacy.domain.po.ResourceRole;
import com.universe.life.user.privacy.domain.vo.ResourceRoleVO;
import com.universe.life.user.privacy.domain.vo.ResourceSimpleVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 资源角色关联表 Mapper 接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-12-01
 */
public interface AdminResourceRoleMapper extends BaseMapper<ResourceRole> {

    /**
     * 查询角色资源
     * @param roleId 角色ID
     * @return 角色资源
     */
    List<ResourceRoleVO> selectRoleResources(Long roleId);

    /**
     * 查询角色选项
     * @param resourceId 资源ID
     * @return 角色选项
     */
    List<RoleOptionVO> selectRoleByResource(Long resourceId);

    /**
     * 检查用户资源权限
     * @param userId 用户ID
     * @param resourceCode 资源编码
     * @return 是否存在
     */
    Long checkUserResourcePermission(Long userId, String resourceCode);

    /**
     * 查询资源列表
     * @param roleIds 角色ID列表
     * @return 资源列表
     */
    List<ResourceSimpleVO> selectResourceSimpleVOList(Set<Long> roleIds);
}
