package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.common.domain.PageResult;
import com.universe.life.model.domain.dto.AdminUserInfoDTO;
import com.universe.life.user.privacy.domain.dao.query.SysUserListQuery;
import com.universe.life.user.privacy.domain.dto.request.*;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.vo.AdminSysUserProfileVO;
import com.universe.life.user.privacy.domain.vo.SysUserDetailVO;
import com.universe.life.user.privacy.domain.vo.SysUserListVO;
import com.universe.life.user.privacy.domain.vo.SysUserOptionVO;

import java.util.List;

/**
 * 平台员工服务接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 创建员工
     *
     * @param request 创建请求
     * @return 员工详情
     */
    SysUserDetailVO createSysUser(SysUserCreateRequest request);

    /**
     * 获取员工详情
     *
     * @param id 员工ID
     * @return 员工详情
     */
    SysUserDetailVO getSysUserById(Long id);

    /**
     * 更新员工信息
     *
     * @param id      员工ID
     * @param request 更新请求
     * @return 员工详情
     */
    SysUserDetailVO updateSysUser(Long id, SysUserUpdateRequest request);

    /**
     * 删除员工
     *
     * @param id 员工ID
     */
    void deleteSysUser(Long id);

    /**
     * 分页查询员工列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SysUserListVO> pageSysUsers(SysUserListQuery query);

    /**
     * 更新员工状态
     *
     * @param id      员工ID
     * @param request 状态更新请求
     */
    void updateSysUserStatus(Long id, SysUserStatusUpdateRequest request);

    /**
     * 重置员工密码
     *
     * @param id      员工ID
     * @param request 密码重置请求
     */
    void resetPassword(Long id, SysUserPasswordResetRequest request);

    /**
     * 获取所有启用的员工选项
     *
     * @return 员工选项列表
     */
    List<SysUserOptionVO> getSysUserOptions();

    /**
     * 更新个人信息
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 员工详情
     */
    SysUserDetailVO updateProfile(Long userId, SysUserProfileUpdateRequest request);

    /**
     * 修改密码
     *
     * @param userId  用户ID
     * @param request 修改请求
     */
    void changePassword(Long userId, SysUserPasswordChangeRequest request);

    /**
     * 登录
     *
     * @param username 用户名
     * @return 用户信息
     */
    AdminUserInfoDTO login(String username);

    /**
     * 获取员工权限列表
     *
     * @param sysUserId 员工ID
     * @return 权限标识列表
     */
    List<String> getSysUserPermissions(Long sysUserId);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    AdminSysUserProfileVO profile();

}
