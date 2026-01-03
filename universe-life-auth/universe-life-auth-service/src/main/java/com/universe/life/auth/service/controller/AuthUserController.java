package com.universe.life.auth.service.controller;

import com.universe.life.auth.service.domain.dto.request.EmployeeCaptchaLoginRequest;
import com.universe.life.auth.service.domain.dto.request.EmployeeLoginRequest;
import com.universe.life.auth.service.domain.dto.request.RegisterFormRequest;
import com.universe.life.auth.service.domain.vo.UserLoginVO;
import com.universe.life.auth.service.service.IAuthUserService;
import com.universe.life.auth.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户权限管理控制器
 * 增强了安全防护机制，包括速率限制和暴力破解防护
 *
 * @author 毛伟然
 * @since 2025/11/13 14:48
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "用户权限管理", description = "提供用户注册、登录等功能，包含安全防护机制")
@Validated
public class AuthUserController {

    private final IAuthUserService authUserService;

    @PostMapping("/register")
    @Operation(
            summary = "用户注册",
            description = "新用户注册，包含验证码校验和速率限制保护"
    )
    public Result<Void> register(@Validated @RequestBody RegisterFormRequest request) {
        log.info("收到用户注册请求，用户标识: {}", request.getIdentification());

        authUserService.register(request);
        log.info("用户注册成功，用户标识: {}", request.getIdentification());
        return Result.success();
    }

    @PostMapping("/employee/login/password")
    @Operation(
            summary = "员工登录",
            description = "员工使用用户名和密码登录，基于 OAuth2 Password 模式下发令牌"
    )
    public Result<UserLoginVO> employeeLogin(@Validated @RequestBody EmployeeLoginRequest request) {
        log.info("收到员工登录请求，用户名: {}", request.getIdentification());

        UserLoginVO loginResult = authUserService.employeeLogin(request);
        log.info("员工登录成功，用户名: {}", request.getIdentification());

        return Result.success(loginResult);
    }

    @PostMapping("/employee/login/captcha")
    @Operation(
            summary = "员工验证码登录",
            description = "员工使用验证码登录，基于 OAuth2 Password 模式下发令牌"
    )
    public Result<UserLoginVO> employeeCaptchaLogin(@Validated @RequestBody EmployeeCaptchaLoginRequest request) {
        log.info("收到员工验证码登录请求，用户标识: {}", request.getIdentification());

        UserLoginVO loginResult = authUserService.employeeCaptchaLogin(request);
        log.info("员工验证码登录成功，用户标识: {}", request.getIdentification());

        return Result.success(loginResult);
    }


}
