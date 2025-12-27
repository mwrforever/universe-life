package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.user.privacy.domain.dto.AdminUserDetailRoleDTO;
import com.universe.life.user.privacy.domain.dto.AdminUserRoleListDTO;
import com.universe.life.user.privacy.domain.po.UserRole;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import com.universe.life.user.privacy.domain.vo.UserRoleDetailVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户角色关联表 Mapper 接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-12-01
 */
public interface AdminUserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查询权限标识列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 获取用户详情角色列表
     * @param id 用户id
     * @return 用户详情角色列表
     */
    List<AdminUserDetailRoleDTO> getUserDetailRoleList(Long id);

    /**
     * 获取用户角色列表
      * @param userIds 用户id列表
     * @return 用户角色列表
     */
    List<AdminUserRoleListDTO> getUserRoleByUserIds(Set<Long> userIds);

    /**
     * 获取用户角色详情列表
     * @param userId 用户 id
     * @return 用户角色详情列表
     */
    List<UserRoleDetailVO> getUserRolesDetail(Long userId);

    /**
     * 获取角色选项VO
     * @param userId 用户 id
     * @return 角色选项VO
     */
    List<RoleOptionVO> getRoleOptionVOList(Long userId);
}
