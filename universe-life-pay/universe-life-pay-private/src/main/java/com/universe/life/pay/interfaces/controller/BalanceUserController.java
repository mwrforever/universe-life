package com.universe.life.pay.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.pay.application.service.BalanceQueryApplicationService;
import com.universe.life.pay.model.dto.BalanceAccountDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户余额", description = "用户端余额查询接口")
@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalanceUserController {

    private final BalanceQueryApplicationService balanceQueryApplicationService;

    @GetMapping("/me")
    public Result<BalanceAccountDTO> me() {
        Long userId = SecurityUtil.getUserId();
        return balanceQueryApplicationService.queryMyBalance(userId);
    }
}
