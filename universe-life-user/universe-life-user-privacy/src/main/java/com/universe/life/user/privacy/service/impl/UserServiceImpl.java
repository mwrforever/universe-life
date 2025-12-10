package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.common.exception.DatabaseException;
import com.universe.life.common.message.ExceptionMessage;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.model.domain.dto.UserStatusDTO;
import com.universe.life.model.enums.UserAuthType;
import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.universe.life.user.privacy.mapper.UserMapper;
import com.universe.life.user.privacy.service.IUserAuthService;
import com.universe.life.user.privacy.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final IUserAuthService userAuthService;

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
}
