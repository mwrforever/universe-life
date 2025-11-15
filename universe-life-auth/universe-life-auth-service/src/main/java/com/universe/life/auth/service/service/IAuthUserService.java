package com.universe.life.auth.service.service;

import com.universe.life.auth.service.domain.dto.request.LoginFormRequest;
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
}
