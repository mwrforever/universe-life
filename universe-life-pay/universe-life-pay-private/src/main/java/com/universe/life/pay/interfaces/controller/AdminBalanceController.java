package com.universe.life.pay.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.pay.application.service.BalanceQueryApplicationService;
import com.universe.life.pay.model.dto.AdminBalanceFlowDTO;
import com.universe.life.pay.model.dto.AdminBalanceFlowListQueryDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端余额", description = "管理端余额流水查询")
@RestController
@RequestMapping("/admin/balance")
@RequiredArgsConstructor
public class AdminBalanceController {

    private final BalanceQueryApplicationService balanceQueryApplicationService;

    @GetMapping("/flows")
    @PreAuthorize("@pm.match('pay:admin:balance:flows')")
    public Result<PageResult<AdminBalanceFlowDTO>> flows(AdminBalanceFlowListQueryDTO query) {
        return balanceQueryApplicationService.pageAdminFlows(query);
    }
}
