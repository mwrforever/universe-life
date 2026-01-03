package com.universe.life.auth.service.service;

import com.universe.life.auth.service.domain.dto.request.EmployeeCaptchaLoginRequest;
import com.universe.life.auth.service.domain.dto.request.EmployeeLoginRequest;
import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;
import com.universe.life.auth.service.domain.vo.UserLoginVO;

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

    /**
     * 员工登录
     *
     * @param request 登录请求
     * @return 登录响应信息
     */
    UserLoginVO employeeLogin(EmployeeLoginRequest request);

    /**
     * 员工验证码登录
     *
     * @param request 验证码登录请求
     * @return 登录响应信息
     */
    UserLoginVO employeeCaptchaLogin(EmployeeCaptchaLoginRequest request);

}
