package com.universe.life.auth.service.controller;

import com.universe.life.auth.resource.domain.dto.request.CaptchaRequest;
import com.universe.life.auth.resource.domain.dto.request.VerifyCodeFormRequest;
import com.universe.life.auth.resource.domain.vo.CaptchaVO;
import com.universe.life.auth.service.service.IAuthCommonService;
import com.universe.life.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证通用接口控制器
 * 提供验证码发送等通用功能，包含安全防护机制
 *
 * @author 毛伟然
 * @since 2025/11/15 21:08
 */
@Slf4j
@RestController
@Tag(name = "认证通用接口", description = "提供验证码发送等通用认证功能")
@RequiredArgsConstructor
@RequestMapping("/common")
@Validated
public class AuthCommonController {

    private final IAuthCommonService authCommonService;

    @PostMapping("/captcha/send")
    @Operation(
            summary = "发送验证码",
            description = "为指定标识（邮箱/手机号）发送验证码，包含速率限制保护"
    )
    public Result<Void> sendCaptcha(@Validated @RequestBody CaptchaRequest request) {
        log.info("收到验证码发送请求，目标: {}", request.getIdentification());
        authCommonService.sendCaptcha(request);
        return Result.success();
    }

    @PostMapping("/captcha/verify")
    @Operation(
            summary = "验证验证码",
            description = "验证指定标识（邮箱/手机号）的验证码是否正确"
    )
    public Result<CaptchaVO> verifyCaptcha(@Validated @RequestBody VerifyCodeFormRequest request) {
        return Result.success(authCommonService.verifyCaptcha(request));
    }

    }
