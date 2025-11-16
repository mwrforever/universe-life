package com.universe.life.auth.service.service;

import com.universe.life.auth.service.domain.dto.request.LoginFormRequest;
import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;
import com.universe.life.auth.service.domain.vo.UserLoginVO;
import com.universe.life.common.domain.Result;

/**
 * @author 毛伟然
 * @since 2025/11/13 14:50
 */
public interface IAuthUserService {
    /**
     * 用户登录
     *
     * @param loginFormRequest 登录表单
     * @return 登录结果
     */
    Result<UserLoginVO> login(LoginFormRequest loginFormRequest);

    /**
     * 用户注册
     *
     * @param request 注册表单
     */
    void register(RegisterFormRequest request);
}
