package com.universe.life.auth.service.service;

import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;

/**
 * @author 毛伟然
 * @since 2025/11/13 14:50
 */
public interface IAuthUserService {

    /**
     * 用户注册
     *
     * @param request 注册表单
     */
    void register(RegisterFormRequest request);


}
