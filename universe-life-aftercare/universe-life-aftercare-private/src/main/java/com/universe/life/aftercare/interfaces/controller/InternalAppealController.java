package com.universe.life.aftercare.interfaces.controller;

import com.universe.life.aftercare.application.service.AftercareAppealService;
import com.universe.life.aftercare.model.dto.AppealSummaryDTO;
import com.universe.life.aftercare.model.dto.CreateAppealDTO;
import com.universe.life.auth.common.domain.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "内部申诉接口", description = "供其他服务调用的申诉内部接口")
@RestController
@RequestMapping("/internal/appeals")
@RequiredArgsConstructor
public class InternalAppealController {

    private final AftercareAppealService appealService;

    @PostMapping
    public Result<Long> createAppeal(@RequestBody CreateAppealDTO dto) {
        return Result.success(appealService.createAppeal(dto));
    }

    @GetMapping("/orders/{orderId}/pending")
    public Result<Boolean> existsPendingAppeal(@PathVariable Long orderId) {
        return Result.success(appealService.existsPendingByOrderId(orderId));
    }

    @GetMapping("/orders/{orderId}/latest-summary")
    public Result<AppealSummaryDTO> getLatestAppealSummary(@PathVariable Long orderId) {
        return Result.success(appealService.getLatestSummaryByOrderId(orderId));
    }

    @DeleteMapping("/{appealId}")
    public Result<Void> cancelAppeal(@PathVariable Long appealId, @RequestParam Long operatorId) {
        appealService.cancelAppeal(appealId, operatorId);
        return Result.success();
    }
}
