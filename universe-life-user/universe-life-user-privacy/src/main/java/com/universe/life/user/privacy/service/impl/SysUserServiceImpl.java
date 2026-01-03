package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.model.domain.dto.AdminUserInfoDTO;
import com.universe.life.user.privacy.domain.dao.query.SysUserListQuery;
import com.universe.life.user.privacy.domain.dto.request.*;
import com.universe.life.user.privacy.domain.po.SysDepartment;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.vo.*;
import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.mapper.AdminResourceMapper;
import com.universe.life.user.privacy.mapper.AdminUserRoleMapper;
import com.universe.life.user.privacy.mapper.SysUserMapper;
import com.universe.life.user.privacy.mapstruct.SysUserMapstruct;
import com.universe.life.user.privacy.service.ISysDepartmentService;
import com.universe.life.user.privacy.service.ISysUserDepartmentService;
import com.universe.life.user.privacy.service.ISysUserService;
import com.universe.life.user.privacy.util.EmployeeNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 平台员工服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final ISysUserDepartmentService userDepartmentService;

    private final AdminUserRoleMapper userRoleMapper;

    private final ISysDepartmentService departmentService;

    private final SysUserMapstruct sysUserMapstruct;

    private final AdminResourceMapper resourceMapper;

    private final PasswordEncoder passwordEncoder;

    private final EmployeeNoGenerator employeeNoGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserDetailVO createSysUser(SysUserCreateRequest request) {
        log.info("创建员工，用户名：{}", request.getUsername());

        // 检查用户名是否已存在
        boolean existsUsername = lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername())
                .exists();
        if (existsUsername) {
            throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.Formatter.dataAlreadyExist("用户名"));
        }

        // 获取主部门编码用于生成工号
        String deptCode = null;
        if (request.getPrimaryDepartmentId() != null) {
            SysDepartment dept = departmentService.getById(request.getPrimaryDepartmentId());
            if (dept != null) {
                deptCode = dept.getDeptCode();
            }
        }

        // 自动生成工号：UL + 部门编码 + 6位序号
        String employeeNo = employeeNoGenerator.generate(deptCode);
        log.info("生成员工工号：{}", employeeNo);

        // 创建员工
        SysUser sysUser = sysUserMapstruct.toPo(request);
        sysUser.setEmployeeNo(employeeNo);
        sysUser.setPassword(passwordEncoder.encode(request.getPassword()));
        boolean saved = save(sysUser);
        if (!saved) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("员工新增"));
        }

        // 分配部门
        if (CollUtil.isNotEmpty(request.getDepartmentIds())) {
            userDepartmentService.assignUserDepartments(
                    sysUser.getId(),
                    request.getDepartmentIds(),
                    request.getPrimaryDepartmentId()
            );
        }

        log.info("创建员工成功，员工ID：{}", sysUser.getId());
        return getSysUserById(sysUser.getId());
    }

    @Override
    public SysUserDetailVO getSysUserById(Long id) {
        SysUser sysUser = getById(id);
        if (ObjectUtil.isNull(sysUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        SysUserDetailVO detailVO = sysUserMapstruct.toDetailVO(sysUser);

        // 查询所属部门
        List<SysDepartmentSimpleVO> departments = userDepartmentService.getUserDepartments(id);
        detailVO.setDepartments(departments);

        // 查询主部门
        SysDepartmentSimpleVO primaryDepartment = userDepartmentService.getUserPrimaryDepartment(id);
        detailVO.setPrimaryDepartment(primaryDepartment);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserDetailVO updateSysUser(Long id, SysUserUpdateRequest request) {
        log.info("更新员工，员工ID：{}", id);

        SysUser existingSysUser = getById(id);
        if (ObjectUtil.isNull(existingSysUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 更新员工基本信息
        SysUser sysUser = sysUserMapstruct.toPo(request);
        sysUser.setId(id);
        boolean updated = updateById(sysUser);
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("员工更新"));
        }

        // 更新部门关联
        if (request.getDepartmentIds() != null) {
            userDepartmentService.assignUserDepartments(
                    id,
                    request.getDepartmentIds(),
                    request.getPrimaryDepartmentId()
            );
        }

        log.info("更新员工成功，员工ID：{}", id);
        return getSysUserById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSysUser(Long id) {
        log.info("删除员工，员工ID：{}", id);

        SysUser sysUser = getById(id);
        if (ObjectUtil.isNull(sysUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 删除部门关联
        userDepartmentService.removeUserDepartments(id);

        // 软删除员工
        removeById(id);

        log.info("删除员工成功，员工ID：{}", id);
    }

    @Override
    public PageResult<SysUserListVO> pageSysUsers(SysUserListQuery query) {
        IPage<SysUserListVO> page = new Page<>(query.getPage(), query.getSize());
        IPage<SysUserListVO> result = baseMapper.selectSysUserList(page, query);

        if (CollUtil.isEmpty(result.getRecords())) {
            return PageResult.empty(page);
        }

        return PageResult.of(result.getRecords(), page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSysUserStatus(Long id, SysUserStatusUpdateRequest request) {
        log.info("更新员工状态，员工ID：{}，状态：{}", id, request.getStatus());

        boolean updated = lambdaUpdate()
                .set(SysUser::getStatus, request.getStatus())
                .eq(SysUser::getId, id)
                .update();

        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("员工状态更新"));
        }

        log.info("更新员工状态成功，员工ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, SysUserPasswordResetRequest request) {
        log.info("重置员工密码，员工ID：{}", id);

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        boolean updated = lambdaUpdate()
                .set(SysUser::getPassword, encodedPassword)
                .eq(SysUser::getId, id)
                .update();

        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("密码重置"));
        }

        log.info("重置员工密码成功，员工ID：{}", id);
    }

    @Override
    public List<SysUserOptionVO> getSysUserOptions() {
        List<SysUser> sysUsers = lambdaQuery()
                .select(SysUser::getId, SysUser::getEmployeeNo, SysUser::getRealName)
                .eq(SysUser::getStatus, CommonStatus.ENABLE)
                .list();

        if (CollUtil.isEmpty(sysUsers)) {
            return new ArrayList<>();
        }

        return sysUserMapstruct.toOptionVOList(sysUsers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserDetailVO updateProfile(Long userId, SysUserProfileUpdateRequest request) {
        log.info("更新个人信息，用户ID：{}", userId);

        SysUser existingUser = getById(userId);
        if (ObjectUtil.isNull(existingUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 更新个人信息（只允许更新部分字段）
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setRealName(request.getRealName());
        updateUser.setPhone(request.getPhone());
        updateUser.setEmail(request.getEmail());
        updateUser.setAvatarUrl(request.getAvatarUrl());
        updateUser.setGender(request.getGender());

        boolean updated = updateById(updateUser);
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("个人信息更新"));
        }

        log.info("更新个人信息成功，用户ID：{}", userId);
        return getSysUserById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, SysUserPasswordChangeRequest request) {
        log.info("修改密码，用户ID：{}", userId);

        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException.ParamException("新密码与确认密码不一致");
        }

        SysUser existingUser = getById(userId);
        if (ObjectUtil.isNull(existingUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 验证原密码是否正确
        if (!passwordEncoder.matches(request.getOldPassword(), existingUser.getPassword())) {
            throw new BusinessException.ParamException("原密码错误");
        }

        // 更新密码
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        boolean updated = lambdaUpdate()
                .set(SysUser::getPassword, encodedPassword)
                .eq(SysUser::getId, userId)
                .update();

        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("密码修改"));
        }

        log.info("修改密码成功，用户ID：{}", userId);
    }

    @Override
    public AdminUserInfoDTO login(String username) {
        // 查询用户密码
        SysUser sysUser = lambdaQuery()
                .select(
                        SysUser::getId,
                        SysUser::getEmployeeNo,
                        SysUser::getPassword,
                        SysUser::getAvatarUrl
                ).eq(SysUser::getEmail, username)
                .or()
                .eq(SysUser::getPhone, username)
                .or()
                .eq(SysUser::getUsername, username)
                .or()
                .eq(SysUser::getEmployeeNo, username)
                .eq(SysUser::getStatus, CommonStatus.ENABLE)
                .one();

        if (ObjectUtil.isNull(sysUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.USER_NOT_FOUND);
        }

        AdminUserInfoDTO result = sysUserMapstruct.toAdminUserInfoDTO(sysUser);

        // 查询用户权限信息
        List<String> permissions = resourceMapper.selectPermissions(sysUser.getId());

        if (CollUtil.isNotEmpty(permissions)) {
            result.setPermissions(permissions);
        }

        return result;

    }

    @Override
    public List<String> getSysUserPermissions(Long sysUserId) {
        log.info("获取员工权限，员工ID：{}", sysUserId);
        return userRoleMapper.selectPermissionsByUserId(sysUserId);
    }

    @Override
    public AdminSysUserProfileVO profile() {
        AdminSysUserProfileVO adminSysUserProfileVO = new AdminSysUserProfileVO();
        adminSysUserProfileVO.setAvatar(SecurityUtil.getAvatar());
        adminSysUserProfileVO.setEmployeeNo(SecurityUtil.getUsername());
        return adminSysUserProfileVO;
    }
}
