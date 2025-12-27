package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.user.privacy.domain.po.SysUserDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;

import java.util.List;

/**
 * 用户部门关联服务接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface ISysUserDepartmentService extends IService<SysUserDepartment> {

    /**
     * 分配用户部门
     *
     * @param userId              用户ID
     * @param departmentIds       部门ID列表
     * @param primaryDepartmentId 主部门ID
     */
    void assignUserDepartments(Long userId, List<Long> departmentIds, Long primaryDepartmentId);

    /**
     * 获取用户的所有部门
     *
     * @param userId 用户ID
     * @return 部门列表
     */
    List<SysDepartmentSimpleVO> getUserDepartments(Long userId);

    /**
     * 获取用户的主部门
     *
     * @param userId 用户ID
     * @return 主部门
     */
    SysDepartmentSimpleVO getUserPrimaryDepartment(Long userId);

    /**
     * 删除用户的所有部门关联
     *
     * @param userId 用户ID
     */
    void removeUserDepartments(Long userId);
}
