package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.user.privacy.domain.po.SysRoleDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门角色关联Mapper接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface SysRoleDepartmentMapper extends BaseMapper<SysRoleDepartment> {

    /**
     * 查询角色关联的部门列表
     *
     * @param roleId 角色ID
     * @return 部门列表
     */
    List<SysDepartmentSimpleVO> selectDepartmentsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询部门关联的角色ID列表
     *
     * @param departmentId 部门ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByDepartmentId(@Param("departmentId") Long departmentId);
}
