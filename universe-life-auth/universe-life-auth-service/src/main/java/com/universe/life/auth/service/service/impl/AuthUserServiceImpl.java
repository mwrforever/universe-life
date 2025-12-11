package com.universe.life.auth.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.universe.life.api.client.UserClient;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;
import com.universe.life.auth.service.service.IAuthUserService;
import com.universe.life.auth.common.exception.AuthException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.model.domain.dto.RegisterFormDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证用户服务实现类
 *
 * <p>提供基于Spring Security Authorization Server的完整认证服务，
 * 支持手动构建OAuth2 token，简化内部项目登录流程。</p>
 *
 * @author 毛伟然
 * @since 2025/11/13 14:51
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements IAuthUserService {

    private final UserClient userClient;
    private final PasswordEncoder bcryptPasswordEncoder;
    private final VerifyCaptchaUtil verifyCaptchaUtil;


    @Override
    public void register(RegisterFormRequest request) {
        // 用户注册
        boolean success = verifyCaptchaUtil.verifyCaptchaIssuer(request.getCaptchaUsageType(), request.getIdentification(), request.getIssuer());
        if (!success) {
            throw new AuthException.AuthenticationException(ExceptionMessage.AUTHORIZATION_CODE_EXPIRED);
        }
        // 封装用户数据
        RegisterFormDTO registerFormDTO = BeanUtil.toBean(request, RegisterFormDTO.class);
        // 对密码进行加密处理
        registerFormDTO.setPassword(bcryptPasswordEncoder.encode(registerFormDTO.getPassword()));
        // 保存用户数据
        userClient.add(registerFormDTO);
    }


}
