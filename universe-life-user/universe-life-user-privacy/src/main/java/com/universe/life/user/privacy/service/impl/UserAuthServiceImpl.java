package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.common.exception.BusinessException;
import com.universe.life.common.exception.SecurityException;
import com.universe.life.common.message.ExceptionMessage;
import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.domain.dto.request.DeleteUserAuthRequest;
import com.universe.life.user.privacy.domain.dto.request.UserAuthCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.UserAuthUpdateRequest;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.universe.life.user.privacy.domain.vo.UserAuthCreateVO;
import com.universe.life.user.privacy.domain.vo.UserAuthListVO;
import com.universe.life.user.privacy.mapper.UserAuthMapper;
import com.universe.life.user.privacy.mapstruct.UserAuthMapstruct;
import com.universe.life.user.privacy.service.IUserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 用户认证表 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl extends ServiceImpl<UserAuthMapper, UserAuth> implements IUserAuthService {

    private final UserAuthMapper userAuthMapper;
    private final UserAuthMapstruct userAuthMapstruct;
    private final VerifyCaptchaUtil verifyCaptchaUtil;
    private final PasswordEncoder bcryptPasswordEncoder;

    @Override
    public UserInfoDTO getUserInfo(String username) {
        UserInfoDTO dto = userAuthMapper.getUserInfo(username);
        // 判断账号是否存在
        if (ObjectUtil.isNull(dto)) {
            return null;
        }
        // 直接返回DTO，不再进行VO转换
        return dto;
    }

    @Override
    public UserAuthCreateVO createUserAuth(UserAuthCreateRequest request) {
        UserAuth userAuth = userAuthMapstruct.toPO(request);
        save(userAuth);
        // 在Service层完成PO到VO的转换
        return userAuthMapstruct.toUserAuthCreateVO(userAuth);
    }

    @Override
    public List<UserAuthListVO> getUserAuthListByUserId(Long userId) {
        List<UserAuth> list = lambdaQuery()
                .select(
                        UserAuth::getId,
                        UserAuth::getIdentificationType,
                        UserAuth::getIdentification
                )
                .eq(UserAuth::getUserId, userId)
                .list();
        // 判空
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        // 在Service层完成PO到VO的转换
        return userAuthMapstruct.toUserAuthListVOList(list);
    }

    @Override
    public void updateUserAuth(UserAuthUpdateRequest request) {
        // 校验验证码
        boolean verified = verifyCaptchaUtil.verifyCaptchaIssuer(
                request.getCaptchaUsageType(),
                request.getIdentification(),
                request.getIssuer()
        );
        // 如果验证码唯一标识验证没有通过
        if (!verified) {
            throw new SecurityException.CaptchaVerificationFailedException(ExceptionMessage.AUTHORIZATION_CODE_INVALID);
        }
        // 获取用户id
        Long userId = SecurityUtil.getUserId();
        // 查询用户信息
        UserAuth userAuth = lambdaQuery()
                .select(
                        UserAuth::getUserId,
                        UserAuth::getIdentification,
                        UserAuth::getPassword,
                        UserAuth::getIdentificationType
                )
                .eq(UserAuth::getUserId, userId)
                .eq(UserAuth::getIdentificationType, request.getIdentificationType())
                .one();
        if (ObjectUtil.isNull(userAuth)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        // 校验用户密码
        if (!bcryptPasswordEncoder.matches(request.getRowPassword(), userAuth.getPassword())) {
            throw new SecurityException.InvalidCredentialsException(ExceptionMessage.PASSWORD_INCORRECT);
        }
        // 更新用户认证信息
        boolean updated = lambdaUpdate()
                .set(UserAuth::getPassword, bcryptPasswordEncoder.encode(request.getNewPassword()))
                .eq(UserAuth::getUserId, userId)
                .update();
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.OPERATION_FAILED);
        }
    }

    @Override
    public void deleteUserAuth(Long id, DeleteUserAuthRequest request) {
        // 查询用户认证数据
        UserAuth userAuth = lambdaQuery()
                .select(UserAuth::getIdentification, UserAuth::getPassword, UserAuth::getIdentificationType)
                .eq(UserAuth::getId, id)
                .one();
        if (ObjectUtil.isNull(userAuth)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        // 验证密码
        if (!bcryptPasswordEncoder.matches(request.getPassword(), userAuth.getPassword())) {
            throw new SecurityException.InvalidCredentialsException(ExceptionMessage.PASSWORD_INCORRECT);
        }
        // 获取用户id
        Long userId = SecurityUtil.getUserId();
        // 统计用户认证方式数量
        Long count = lambdaQuery()
                .eq(UserAuth::getUserId, userId)
                .count();
        if (count <= 1) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.OPERATION_NOT_ALLOWED);
        }
        // 删除用户认证方式
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.OPERATION_FAILED);
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        // TODO
        List<UserAuth> list = lambdaQuery()
                .select(UserAuth::getId)
                .eq(UserAuth::getUserId, userId)
                .list();

        if (CollUtil.isNotEmpty(list)) {
            List<Long> ids = list.stream().map(UserAuth::getId).toList();
            this.removeByIds(ids);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByUserIds(List<Long> userIds) {
        if (ObjectUtil.isNotEmpty(userIds)) {
            List<UserAuth> list = lambdaQuery()
                    .select(UserAuth::getId)
                    .in(UserAuth::getUserId, userIds)
                    .list();

            if (CollUtil.isNotEmpty(list)) {
                List<Long> ids = list.stream().map(UserAuth::getId).toList();
                this.removeByIds(ids);
            }
        }
    }
}
