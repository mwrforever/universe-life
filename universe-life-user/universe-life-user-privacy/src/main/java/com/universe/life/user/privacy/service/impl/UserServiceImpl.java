package com.universe.life.user.privacy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.model.domain.dto.RegisterFormDTO;
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
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final IUserAuthService userAuthService;

    @Override
    @Transactional
    public void add(RegisterFormDTO registerFormDTO) {
        // 封装实体对象
        User user = new User();
        user.setUsername(registerFormDTO.getUsername());
        // 保存用户数据
        save(user);
        // 保存用户认证数据
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(user.getId());
        userAuth.setIdentification(registerFormDTO.getIdentification());
        userAuth.setIdentificationType(registerFormDTO.getIdentificationType());
        userAuth.setPassword(registerFormDTO.getPassword());

        // 保存用户名认证关系
        UserAuth userAuthDefault = new UserAuth();
        userAuthDefault.setUserId(user.getId());
        userAuthDefault.setIdentification(user.getUsername());
        userAuthDefault.setIdentificationType(UserAuthType.USERNAME);
        userAuthDefault.setPassword(registerFormDTO.getPassword());

        // 保存映射关系
        userAuthService.saveBatch(List.of(userAuth, userAuthDefault));
    }
}
