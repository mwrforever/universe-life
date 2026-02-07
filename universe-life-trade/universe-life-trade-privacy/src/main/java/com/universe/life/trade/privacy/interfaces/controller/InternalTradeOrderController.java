package com.universe.life.trade.privacy.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.trade.model.dto.DisputeOrderDTO;
import com.universe.life.trade.privacy.application.service.TradeOrderApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "内部交易订单接口", description = "供其他服务调用的内部交易订单接口")
@RestController
@RequestMapping("/internal/trade/orders")
@RequiredArgsConstructor
public class InternalTradeOrderController {

    private final TradeOrderApplicationService orderApplicationService;

    @PostMapping("/dispute")
    public Result<Void> disputeOrder(@RequestBody DisputeOrderDTO dto) {
        orderApplicationService.disputeOrder(dto.getOrderId(), dto.getOperatorId());
        return Result.success();
    }
}
