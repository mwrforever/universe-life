package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.exception.DatabaseException;
import com.universe.life.auth.common.exception.SecurityException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.user.privacy.domain.dao.query.AdminUserListQuery;
import com.universe.life.user.privacy.domain.dto.AdminUserDetailDTO;
import com.universe.life.user.privacy.domain.dto.AdminUserDetailRoleDTO;
import com.universe.life.user.privacy.domain.dto.request.*;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.universe.life.user.privacy.domain.po.UserDetail;
import com.universe.life.user.privacy.domain.vo.AdminUserDetailVO;
import com.universe.life.user.privacy.domain.vo.AdminUserListVO;
import com.universe.life.user.privacy.domain.vo.UserStatusVO;
import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.mapper.AdminUserMapper;
import com.universe.life.user.privacy.mapper.AdminUserRoleMapper;
import com.universe.life.user.privacy.mapstruct.UserAuthMapstruct;
import com.universe.life.user.privacy.mapstruct.UserMapstruct;
import com.universe.life.user.privacy.service.IAdminUserService;
import com.universe.life.user.privacy.service.ISysUserService;
import com.universe.life.user.privacy.service.IUserAuthService;
import com.universe.life.user.privacy.service.IUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员用户服务实现类
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, User> implements IAdminUserService {

    private final IUserAuthService userAuthService;
    private final UserMapstruct userMapstruct;
    private final UserAuthMapstruct userAuthMapstruct;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final PasswordEncoder bcryptPasswordEncoder;
    private final ISysUserService sysUserService;
    private final IUserDetailService userDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserListVO createUser(UserCreateRequest request) {
        log.info("创建用户开始，用户名：{}", request.getUsername());
        // 直接保存用户数据
        User po = userMapstruct.toPO(request);
        // 保存用户信息
        boolean saved = save(po);
        if (!saved) {
            throw new DatabaseException.UpdateException(ExceptionMessage.OPERATION_FAILED);
        }
        List<UserAuth> userAuths = request.getUserAuthList().stream().map(ua -> {
            UserAuth userAuth = userAuthMapstruct.userAuthRequestToPo(ua);
            return userAuth.setUserId(po.getId());
        }).toList();
        // 保存用户认证信息
        userAuthService.saveBatch(userAuths);
        // 构建返回结果
        return userMapstruct.toAdminUserListVO(po);
    }

    @Override
    public AdminUserDetailVO getUserById(Long id) {
        log.info("管理员查询用户，用户ID：{}", id);
        // 先查用户信息
        User po = getById(id);
        if (ObjectUtil.isNull(po)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        // 封装成DTO对象
        AdminUserDetailDTO adminUserDetailDTO = userMapstruct.toAdminUserDetailDTO(po);
        // 再查寻用户角色信息
        List<AdminUserDetailRoleDTO> roleDTOList = adminUserRoleMapper.getUserDetailRoleList(id);
        // 在Service层完成DTO到VO的转换
        if (CollUtil.isNotEmpty(roleDTOList)) {
            adminUserDetailDTO.setRoles(roleDTOList);
        }
        return userMapstruct.toAdminUserDetailVO(adminUserDetailDTO);
    }

    @Override
    public PageResult<User> pageUsers(AdminUserListQuery query) {
        // 复杂查询使用DTO，连表查询用户列表和角色信息
        IPage<User> page = new Page<>(query.getPage(), query.getSize());
        // 查询用户列表
        IPage<User> result = lambdaQuery().like(StrUtil.isNotBlank(query.getUsername()), User::getUsername, query.getUsername()).gt(ObjectUtil.isNotNull(query.getStartTime()), User::getCreatedAt, query.getStartTime()).lt(ObjectUtil.isNotNull(query.getEndTime()), User::getCreatedAt, query.getEndTime()).eq(ObjectUtil.isNotNull(query.getGender()), User::getGender, query.getGender()).eq(ObjectUtil.isNotNull(query.getStatus()), User::getStatus, query.getStatus()).orderByDesc(User::getCreatedAt).page(page);
        // 获取用户列表
        List<User> records = result.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageResult.empty(page);
        }
        return PageResult.of(records, result);
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        log.info("更新用户信息，用户ID：{}", id);
        // 将请求参数转换成PO对象
        User user = userMapstruct.toPoByUserUpdateRequest(request);
        // 更新用户信息
        user.setLastLoginIp(null);
        user.setLastLoginAt(null);
        updateById(user);
        log.info("更新用户信息成功，用户ID：{}", id);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, PasswordUserRequest request) {
        log.info("删除用户，用户ID：{}", id);
        checkUserByPassword(request);
        // 删除用户
        removeById(id);
        // 删除用户认证信息
        boolean removed = userAuthService.lambdaUpdate().eq(UserAuth::getUserId, id).remove();
        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户认证信息删除"));
        }
        // 删除用户详细信息
        boolean detailRemoved = userDetailService.lambdaUpdate().eq(UserDetail::getId, id).remove();
        if (detailRemoved) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户详细信息删除"));
        }
        log.info("删除用户成功，用户ID：{}", id);
    }

    @Override
    public UserStatusVO getUserStatus(String username) {
        User user = lambdaQuery().select(User::getId, User::getUsername, User::getStatus).eq(User::getUsername, username).one();
        // 判空
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        // 在Service层完成枚举到VO的转换
        return userMapstruct.toUserStatusVO(user.getStatus());
    }

    @Override
    public void resetPassword(Long id, ResetPasswordRequest request) {
        log.info("重置用户密码，用户ID：{}", id);
        boolean updated = userAuthService.lambdaUpdate().set(UserAuth::getPassword, bcryptPasswordEncoder.encode(request.getNewPassword())).eq(UserAuth::getUserId, id).update();
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.OPERATION_FAILED);
        }
        log.info("重置用户密码成功，用户ID：{}", id);
    }

    @Override
    public void updateUserStatus(UserStatusUpdateRequest request) {
        log.info("更新用户状态，用户ID：{}，状态：{}", request.getId(), request.getStatus());
        // 更新用户状态
        boolean updated = lambdaUpdate().set(User::getStatus, request.getStatus()).eq(User::getId, request.getId()).update();
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.OPERATION_FAILED);
        }
        log.info("更新用户状态成功，用户ID：{}", request.getId());
    }

    @Override
    @Transactional
    public void batchDeleteUsers(List<Long> ids, PasswordUserRequest request) {
        log.info("批量删除用户，用户ID列表：{}", ids);
        // 获取需要删除的用户
        checkUserByPassword(request);
        removeByIds(ids);
        boolean removed = userAuthService.lambdaUpdate().in(UserAuth::getUserId, ids).remove();
        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户认证信息批量删除"));
        }
        removed = userDetailService.lambdaUpdate().in(UserDetail::getId, ids).remove();
        if (removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户详细信息批量删除"));
        }
        log.info("批量删除用户成功，删除数量：{}", ids.size());
    }

    private void checkUserByPassword(PasswordUserRequest request) {
        Long userId = SecurityUtil.getUserId();
        SysUser sysUser = sysUserService.lambdaQuery().select(SysUser::getPassword).eq(SysUser::getId, userId).one();
        if (ObjectUtil.isNull(sysUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        if (!bcryptPasswordEncoder.matches(request.getPassword(), sysUser.getPassword())) {
            throw new SecurityException.InvalidCredentialsException(ExceptionMessage.PASSWORD_INCORRECT);
        }
    }

    @Override
    public void batchUpdateUserStatus(UserBatchStatusUpdateRequest request) {
        log.info("批量更新用户状态，用户ID列表：{}，状态：{}", request.getUserIds(), request.getStatus());
        // 直接更新用户状态
        boolean updated = lambdaUpdate().set(User::getStatus, request.getStatus()).in(User::getId, request.getUserIds()).update();
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.OPERATION_FAILED);
        }
        log.info("批量更新用户状态成功，更新数量：{}", request.getUserIds().size());
    }

    @Override
    public Boolean checkPassword(PasswordUserRequest request) {
        // 获取当前用户密码
        SysUser sysUser = sysUserService.lambdaQuery().select(SysUser::getPassword).eq(SysUser::getId, SecurityUtil.getUserId()).eq(SysUser::getStatus, CommonStatus.ENABLE).one();
        if (ObjectUtil.isNull(sysUser)) {
            throw new DatabaseException.QueryException(ExceptionMessage.Formatter.operationFailed("您的账户出现异常，请刷新重试"));
        }
        // 校验用户密码
        if (!bcryptPasswordEncoder.matches(request.getPassword(), sysUser.getPassword())) {
            throw new SecurityException.InvalidCredentialsException(ExceptionMessage.PASSWORD_INCORRECT);
        }
        return true;
    }
}
