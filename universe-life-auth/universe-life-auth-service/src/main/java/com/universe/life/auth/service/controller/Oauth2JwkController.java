package com.universe.life.auth.service.controller;


import com.universe.life.auth.service.service.IOauth2JwkService;
import com.universe.life.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * JWK表：存储JWK密钥对，用于签名和验证JWT令牌 前端控制器
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
@RestController
@RequestMapping("/oauth2-jwk")
@Tag(name = "JWK相关接口", description = "JWK表：存储JWK密钥对，用于签名和验证JWT令牌")
@RequiredArgsConstructor
public class Oauth2JwkController {

    private final IOauth2JwkService oauth2JwkService;

    @PostMapping("/update")
    @Operation(description = "管理员强制更新JWK密钥对")
    public Result<Object> update(@RequestBody String password) {
        return Result.success(oauth2JwkService.update(password));
    }

}
