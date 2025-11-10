package com.universe.life.user.privacy.controller;


import com.universe.life.user.privacy.service.IUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 系统用户表 前端控制器
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-03
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(description = "用户管理", name = "用户管理")
public class UserController {

    private final IUserService userService;


}
