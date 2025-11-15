package com.universe.life.auth.service.controller;

import com.universe.life.auth.service.domain.dto.request.LoginFormRequest;
import com.universe.life.auth.service.domain.vo.UserLoginVO;
import com.universe.life.auth.service.service.IAuthUserService;
import com.universe.life.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 毛伟然
 * @since 2025/11/13 14:48
 */
@RestController
@RequestMapping("/auth/user")
@RequiredArgsConstructor
@Tag(name = "用户权限管理")
@Validated
public class AuthUserController {

    private final IAuthUserService authUserService;

    @PostMapping("/register")
    @Operation(description = "用户注册")
    public Result<Void> register() {
        return null;
    }

    @PostMapping("/login")
    @Operation(description = "用户登录")
    public Result<UserLoginVO> login(@RequestBody @Validated LoginFormRequest loginFormRequest) {
        return authUserService.login(loginFormRequest);
    }

}
