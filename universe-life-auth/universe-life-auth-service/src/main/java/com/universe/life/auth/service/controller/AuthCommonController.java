package com.universe.life.auth.service.controller;

import com.universe.life.auth.resource.domain.dto.request.CaptchaRequest;
import com.universe.life.auth.resource.domain.vo.CaptchaVO;
import com.universe.life.auth.service.service.IAuthCommonService;
import com.universe.life.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:08
 */
@RestController
@Tag(name = "AuthCommonController", description = "通用接口")
@RequiredArgsConstructor
@RequestMapping("/auth/common")
public class AuthCommonController {

    private final IAuthCommonService authCommonService;

    @PostMapping("/captcha/send")
    @Operation(description = "获取验证码")
    public Result<CaptchaVO> sendCaptcha(CaptchaRequest request) {

        return Result.success(authCommonService.sendCaptcha(request));
    }


}
