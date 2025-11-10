package com.universe.life.user.privacy.controller;


import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.service.IUserAuthService;
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
 * @since 2025-11-03
 */
@RestController
@RequestMapping("/users/auth")
@RequiredArgsConstructor
@Tag(description = "用户认证表", name = "用户认证表")
public class UserAuthController {

    private final IUserAuthService userAuthService;

    @GetMapping("/getUserInfo")
    public UserInfoDTO getUserInfo(String username) {
        return userAuthService.getUserInfo(username);
    }
}
