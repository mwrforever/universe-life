package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.user.privacy.domain.po.SysUserDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.domain.vo.SysUserSimpleVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户部门关联Mapper接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface SysUserDepartmentMapper extends BaseMapper<SysUserDepartment> {

    /**
     * 查询用户的所有部门
     *
     * @param userId 用户ID
     * @return 部门列表
     */
    List<SysDepartmentSimpleVO> selectDepartmentsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的主部门
     *
     * @param userId 用户ID
     * @return 主部门
     */
    SysDepartmentSimpleVO selectPrimaryDepartmentByUserId(@Param("userId") Long userId);
    
    /**
     * 查询部门的所有用户
     *
     * @param departmentId 部门ID
     * @return 用户列表
     */
    List<SysUserSimpleVO> selectUsersByDepartmentId(@Param("departmentId") Long departmentId);
}
