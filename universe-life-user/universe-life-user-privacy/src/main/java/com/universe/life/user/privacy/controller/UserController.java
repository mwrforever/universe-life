package com.universe.life.user.privacy.controller;


import com.universe.life.model.domain.dto.RegisterFormDTO;
import com.universe.life.user.privacy.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 系统用户表 前端控制器
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Tag(name = "用户接口")
@RequiredArgsConstructor
@RestController
@Validated
public class UserController {

    private final IUserService userService;

    @PostMapping("/add")
    @Operation(description = "添加用户")
    public void add(@RequestBody RegisterFormDTO registerFormDTO) {
        userService.add(registerFormDTO);
    }

}
