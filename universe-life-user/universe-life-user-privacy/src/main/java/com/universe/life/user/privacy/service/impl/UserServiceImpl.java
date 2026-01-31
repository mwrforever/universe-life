package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.exception.DatabaseException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.model.domain.dto.UserStatusDTO;
import com.universe.life.model.enums.UserAuthType;
import com.universe.life.user.privacy.constants.RedisConstants;
import com.universe.life.user.privacy.domain.dto.request.RegisterFormRequest;
import com.universe.life.user.privacy.domain.dto.request.UserProfileUpdateRequest;
import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.universe.life.user.privacy.domain.vo.UserInfoVO;
import com.universe.life.user.privacy.mapper.UserMapper;
import com.universe.life.user.privacy.mapstruct.UserMapstruct;
import com.universe.life.user.privacy.service.IUserAuthService;
import com.universe.life.user.privacy.service.IUserService;
import com.universe.life.common.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final VerifyCaptchaUtil verifyCaptchaUtil;
    private final PasswordEncoder bcryptPasswordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final CacheUtil cacheUtil;


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
    public void register(RegisterFormRequest request) {
        // 用户注册
        String key = verifyCaptchaUtil.verifyCaptchaIssuerNotDelete(request.getCaptchaUsageType(), request.getIdentification(), request.getIssuer());
        if (ObjectUtil.isNull(key)) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTHORIZATION_CODE_EXPIRED);
        }
        // 封装用户数据
        User user = new User();
        user.setUsername(request.getUsername());
        // 保存用户数据
        boolean userSaved = save(user);
        if (!userSaved) {
            throw new DatabaseException.UpdateException(ExceptionMessage.Formatter.operationFailed("用户添加失败，用户名可能重复"));
        }
        // 对密码进行加密处理
        String encodePassword = bcryptPasswordEncoder.encode(request.getPassword());
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(user.getId());
        userAuth.setIdentification(request.getIdentification());
        userAuth.setIdentificationType(request.getIdentificationType());
        userAuth.setPassword(encodePassword);

        UserAuth userAuthDefault = new UserAuth();
        userAuthDefault.setUserId(user.getId());
        userAuthDefault.setIdentification(user.getUsername());
        userAuthDefault.setIdentificationType(UserAuthType.USERNAME);
        userAuthDefault.setPassword(encodePassword);

        boolean saved = userAuthService.saveBatch(List.of(userAuth, userAuthDefault));
        if (!saved) {
            throw new DatabaseException.UpdateException(ExceptionMessage.Formatter.operationFailed("用户认证信息添加失败，请检查手机号或邮箱是否已经注册"));
        }
        stringRedisTemplate.delete(key);
    }

    @Override
    public UserInfoVO getUserById(Long userId) {
        // 1. 尝试从缓存获取
        String cacheKey = RedisConstants.USER_INFO_KEY + userId;
        UserInfoVO cached = cacheUtil.get(cacheKey, UserInfoVO.class);
        if (cached != null) {
            return cached;
        }
        
        // 2. 缓存未命中，查询数据库
        User user = getById(userId);
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }
        
        // 3. 转换为 VO
        UserInfoVO userInfoVO = userMapstruct.toUserInfoVO(user);
        
        // 4. 写入缓存
        cacheUtil.set(cacheKey, userInfoVO, RedisConstants.getUserInfoExpire());
        
        return userInfoVO;
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
        
        // 删除缓存
        String cacheKey = RedisConstants.USER_INFO_KEY + userId;
        cacheUtil.delete(cacheKey);

        log.info("更新用户个人信息成功，用户ID：{}", userId);
        return getUserById(userId);
    }
}
