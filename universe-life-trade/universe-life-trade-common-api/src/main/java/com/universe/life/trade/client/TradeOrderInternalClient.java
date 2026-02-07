package com.universe.life.trade.client;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.trade.model.dto.DisputeOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "trade-service", contextId = "tradeOrderInternalClient")
public interface TradeOrderInternalClient {

    @PostMapping("/internal/trade/orders/dispute")
    Result<Void> disputeOrder(@RequestBody DisputeOrderDTO dto);
}
