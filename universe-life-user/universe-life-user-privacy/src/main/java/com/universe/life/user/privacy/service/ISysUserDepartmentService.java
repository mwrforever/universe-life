package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.user.privacy.domain.dto.request.BatchUserDepartmentRequest;
import com.universe.life.user.privacy.domain.dto.request.UserDepartmentRequest;
import com.universe.life.user.privacy.domain.po.SysUserDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.domain.vo.SysUserSimpleVO;

import java.util.List;

/**
 * 用户部门关联服务接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface ISysUserDepartmentService extends IService<SysUserDepartment> {

    /**
     * 添加用户到部门
     *
     * @param request 用户部门关联请求
     */
    void addUserToDepartment(UserDepartmentRequest request);

    /**
     * 从部门移除用户
     *
     * @param userId       用户ID
     * @param departmentId 部门ID
     */
    void removeUserFromDepartment(Long userId, Long departmentId);

    /**
     * 批量添加用户到部门
     *
     * @param request 批量用户部门关联请求
     */
    void batchAddUsersToDepartment(BatchUserDepartmentRequest request);

    /**
     * 批量从部门移除用户
     *
     * @param request 批量用户部门关联请求
     */
    void batchRemoveUsersFromDepartment(BatchUserDepartmentRequest request);

    /**
     * 获取部门的所有用户
     *
     * @param departmentId 部门ID
     * @return 用户列表
     */
    List<SysUserSimpleVO> getUsersByDepartment(Long departmentId);

    /**
     * 获取用户的所有部门
     *
     * @param userId 用户ID
     * @return 部门列表
     */
    List<SysDepartmentSimpleVO> getDepartmentsByUser(Long userId);

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
