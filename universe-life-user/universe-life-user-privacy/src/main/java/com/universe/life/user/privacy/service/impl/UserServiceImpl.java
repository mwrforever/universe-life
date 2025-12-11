package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.exception.DatabaseException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.domain.dto.UserStatusDTO;
import com.universe.life.model.enums.UserAuthType;
import com.universe.life.user.privacy.domain.dto.request.UserProfileUpdateRequest;
import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.universe.life.user.privacy.domain.vo.UserInfoVO;
import com.universe.life.user.privacy.mapper.UserMapper;
import com.universe.life.user.privacy.mapstruct.UserMapstruct;
import com.universe.life.user.privacy.service.IUserAuthService;
import com.universe.life.user.privacy.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final IUserAuthService userAuthService;
    private final UserMapstruct userMapstruct;

    @Override
    public UserStatusDTO getStatusByUsername(String username) {
        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .select(User::getStatus)
                .one();

        if (ObjectUtil.isNull(user)) {
            throw new DatabaseException.QueryException(ExceptionMessage.DATA_NOT_FOUND);
        }
        return new UserStatusDTO(user.getStatus());
    }

    @Override
    @Transactional
    public void add(RegisterFormDTO registerFormDTO) {
        // 保留原有的用户添加逻辑
        User user = new User();
        user.setUsername(registerFormDTO.getUsername());
        save(user);

        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(user.getId());
        userAuth.setIdentification(registerFormDTO.getIdentification());
        userAuth.setIdentificationType(registerFormDTO.getIdentificationType());
        userAuth.setPassword(registerFormDTO.getPassword());

        UserAuth userAuthDefault = new UserAuth();
        userAuthDefault.setUserId(user.getId());
        userAuthDefault.setIdentification(user.getUsername());
        userAuthDefault.setIdentificationType(UserAuthType.USERNAME);
        userAuthDefault.setPassword(registerFormDTO.getPassword());

        userAuthService.saveBatch(List.of(userAuth, userAuthDefault));
    }

    @Override
    public UserInfoVO getUserById(Long userId) {
        User user = getById(userId);
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        return userMapstruct.toUserInfoVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        log.info("更新用户个人信息，用户ID：{}", userId);

        User existingUser = getById(userId);
        if (ObjectUtil.isNull(existingUser)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 如果要更新用户名，检查是否已存在
        if (StrUtil.isNotBlank(request.getUsername())
                && !request.getUsername().equals(existingUser.getUsername())) {
            boolean existsUsername = lambdaQuery()
                    .eq(User::getUsername, request.getUsername())
                    .ne(User::getId, userId)
                    .exists();
            if (existsUsername) {
                throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.Formatter.dataAlreadyExist("用户名"));
            }
        }

        // 更新用户信息
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUsername(request.getUsername());
        updateUser.setAvatarUrl(request.getAvatarUrl());
        updateUser.setGender(request.getGender());

        boolean updated = updateById(updateUser);
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户信息更新"));
        }

        log.info("更新用户个人信息成功，用户ID：{}", userId);
        return getUserById(userId);
    }
}
