package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.model.domain.dto.AdminUserInfoDTO;
import com.universe.life.user.privacy.constants.RedisConstants;
import com.universe.life.user.privacy.domain.dao.query.SysUserListQuery;
import com.universe.life.user.privacy.domain.dto.request.*;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.vo.*;
import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.enums.VerificationType;
import com.universe.life.user.privacy.mapper.AdminResourceMapper;
import com.universe.life.user.privacy.mapper.AdminUserRoleMapper;
import com.universe.life.user.privacy.mapper.SysUserMapper;
import com.universe.life.user.privacy.mapstruct.SysUserMapstruct;
import com.universe.life.user.privacy.service.ISysUserDepartmentService;
import com.universe.life.user.privacy.service.ISysUserService;
import com.universe.life.common.util.CacheUtil;
import lombok.NonNull;
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

    private static final String EMPLOYEE_NO_PREFIX = "UL";

    private final ISysUserDepartmentService userDepartmentService;
    private final AdminUserRoleMapper userRoleMapper;
    private final SysUserMapstruct sysUserMapstruct;
    private final AdminResourceMapper resourceMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerifyCaptchaUtil verifyCaptchaUtil;
    private final CacheUtil cacheUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSysUser(SysUserCreateRequest request) {
        log.info("创建员工，用户名：{}", request.getUsername());

        // 检查用户名是否已存在
        boolean existsUsername = lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername())
                .exists();
        if (existsUsername) {
            throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.Formatter.dataAlreadyExist("用户名"));
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (StrUtil.isNotBlank(request.getEmail())) {
            boolean existsEmail = lambdaQuery()
                    .eq(SysUser::getEmail, request.getEmail())
                    .exists();
            if (existsEmail) {
                throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.Formatter.dataAlreadyExist("邮箱"));
            }
        }

        // 检查手机号是否已存在（如果提供了手机号）
        if (StrUtil.isNotBlank(request.getPhone())) {
            boolean existsPhone = lambdaQuery()
                    .eq(SysUser::getPhone, request.getPhone())
                    .exists();
            if (existsPhone) {
                throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.Formatter.dataAlreadyExist("手机号"));
            }
        }

        // 创建员工
        SysUser sysUser = sysUserMapstruct.toPo(request);

        // 自动生成工号：UL + 部门编码 + 6位序号
        if (StrUtil.isNotBlank(request.getPrimaryDepartmentCode())) {
            String employeeNo = getEmployeeNo(request.getPrimaryDepartmentCode());
            log.info("生成员工工号：{}", employeeNo);
            sysUser.setEmployeeNo(employeeNo);
        }

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
    }

    private static @NonNull String getEmployeeNo(String departmentCode) {
        return EMPLOYEE_NO_PREFIX + departmentCode + System.currentTimeMillis();
    }

    @Override
    public SysUserDetailVO getSysUserById(Long id) {
        // 1. 尝试从缓存获取
        String cacheKey = RedisConstants.SYS_USER_INFO_KEY + id;
        SysUserDetailVO cached = cacheUtil.get(cacheKey, SysUserDetailVO.class);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中，查询数据库
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

        // 3. 写入缓存
        cacheUtil.set(cacheKey, detailVO, RedisConstants.getSysUserInfoExpire());

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSysUser(Long id, SysUserUpdateRequest request) {
        log.info("更新员工，员工ID：{}", id);

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

        // 删除缓存
        String cacheKey = RedisConstants.SYS_USER_INFO_KEY + id;
        cacheUtil.delete(cacheKey);

        log.info("更新员工成功，员工ID：{}", id);
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

        // 删除缓存
        String cacheKey = RedisConstants.SYS_USER_INFO_KEY + id;
        cacheUtil.delete(cacheKey);

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
    public List<SysUserOptionVO> getSysUserOptions(String keyword) {
        Page<SysUser> page = lambdaQuery()
                .select(SysUser::getId, SysUser::getEmployeeNo, SysUser::getRealName)
                .eq(SysUser::getStatus, CommonStatus.ENABLE)
                .like(SysUser::getEmployeeNo, keyword)
                .or()
                .like(SysUser::getPhone, keyword)
                .or()
                .like(SysUser::getEmail, keyword)
                .or()
                .like(SysUser::getUsername, keyword)
                .page(new Page<>(1, 5));

        List<SysUser> records = page.getRecords();

        if (CollUtil.isEmpty(records)) {
            return new ArrayList<>();
        }

        return sysUserMapstruct.toOptionVOList(records);
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
    public SysUserPersonProfileVO getPersonProfile() {
        Long currentUserId = SecurityUtil.getUserId();
        log.info("获取当前用户个人资料，用户ID：{}", currentUserId);

        SysUser sysUser = getById(currentUserId);
        if (ObjectUtil.isNull(sysUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        SysUserPersonProfileVO profileVO = sysUserMapstruct.toPersonProfileVO(sysUser);

        // 查询所属部门
        List<SysDepartmentSimpleVO> departments = userDepartmentService.getUserDepartments(currentUserId);
        profileVO.setDepartments(departments);

        // 查询主部门
        SysDepartmentSimpleVO primaryDepartment = userDepartmentService.getUserPrimaryDepartment(currentUserId);
        profileVO.setPrimaryDepartment(primaryDepartment);

        return profileVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePersonProfile(SysUserPersonProfileUpdateRequest request) {
        Long currentUserId = SecurityUtil.getUserId();
        log.info("更新当前用户个人资料，用户ID：{}", currentUserId);

        // 更新个人资料
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUserId);
        updateUser.setUsername(request.getUsername());
        updateUser.setRealName(request.getRealName());
        updateUser.setPhone(request.getPhone());
        updateUser.setEmail(request.getEmail());
        updateUser.setAvatarUrl(request.getAvatarUrl());
        updateUser.setGender(request.getGender());

        boolean updated = updateById(updateUser);
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("个人资料更新"));
        }

        log.info("更新当前用户个人资料成功，用户ID：{}", currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePersonPassword(SysUserPersonPasswordUpdateRequest request) {
        Long currentUserId = SecurityUtil.getUserId();
        log.info("当前用户修改密码，用户ID：{}，验证方式：{}", currentUserId, request.getVerificationType());

        SysUser existingUser = getById(currentUserId);
        if (ObjectUtil.isNull(existingUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 根据验证方式进行验证
        VerificationType verificationType = request.getVerificationType();
        if (verificationType.isPassword()) {
            checkPassword(request, existingUser);
        } else if (verificationType.isCaptcha()) {
            checkCaptcha(request, verificationType, existingUser);
        }

        // 更新密码
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        boolean updated = lambdaUpdate()
                .set(SysUser::getPassword, encodedPassword)
                .eq(SysUser::getId, currentUserId)
                .update();

        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("密码修改"));
        }

        log.info("当前用户修改密码成功，用户ID：{}", currentUserId);
    }

    private void checkPassword(SysUserPersonPasswordUpdateRequest request, SysUser existingUser) {
        // 原密码验证
        if (StrUtil.isBlank(request.getCurrentPassword())) {
            throw new BusinessException.ParamException("当前密码不能为空");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), existingUser.getPassword())) {
            throw new BusinessException.ParamException("当前密码错误");
        }
    }

    private void checkCaptcha(SysUserPersonPasswordUpdateRequest request, VerificationType verificationType, SysUser existingUser) {
        // 验证码验证
        if (StrUtil.isBlank(request.getCaptcha())) {
            throw new BusinessException.ParamException("验证码不能为空");
        }
        if (request.getCaptchaUsageType() == null) {
            throw new BusinessException.ParamException("验证码用途类型不能为空");
        }

        // 根据验证方式获取对应的标识（邮箱或手机号）
        String identification = verificationType == VerificationType.EMAIL_CAPTCHA
                ? existingUser.getEmail()
                : existingUser.getPhone();

        if (StrUtil.isBlank(identification)) {
            String identityType = verificationType == VerificationType.EMAIL_CAPTCHA ? "邮箱" : "手机号";
            throw new BusinessException.ParamException("用户未绑定" + identityType);
        }

        // 验证验证码
        boolean verified = verifyCaptchaUtil.verifyCaptcha(
                request.getCaptchaUsageType(),
                identification,
                request.getCaptcha()
        );
        if (!verified) {
            throw new BusinessException.ParamException("验证码错误或已过期");
        }
    }
}
