package com.universe.life.user.privacy.controller;


import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.service.IUserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户认证表 前端控制器
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证接口")
public class UserAuthController {

    private final IUserAuthService userAuthService;


    @GetMapping("/getUserInfo")
    @Operation(description = "获取用户信息")
    public UserInfoDTO getUserInfo(String username) {
        return userAuthService.getUserInfo(username);
    }

}
