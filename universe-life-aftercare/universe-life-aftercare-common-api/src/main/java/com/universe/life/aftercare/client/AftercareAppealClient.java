package com.universe.life.aftercare.client;

import com.universe.life.aftercare.model.dto.AppealSummaryDTO;
import com.universe.life.aftercare.model.dto.CreateAppealDTO;
import com.universe.life.auth.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "aftercare-service", contextId = "aftercareAppealClient")
public interface AftercareAppealClient {

    @PostMapping("/internal/appeals")
    Result<Long> createAppeal(@RequestBody CreateAppealDTO dto);

    @GetMapping("/internal/appeals/biz/{bizType}/{bizId}/pending")
    Result<Boolean> existsPendingAppeal(@PathVariable("bizType") Integer bizType, @PathVariable("bizId") Long bizId);

    @GetMapping("/internal/appeals/biz/{bizType}/{bizId}/latest-summary")
    Result<AppealSummaryDTO> getLatestAppealSummary(@PathVariable("bizType") Integer bizType, @PathVariable("bizId") Long bizId);
}
