package com.universe.life.pay.client;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.pay.model.dto.CreatePaymentDTO;
import com.universe.life.pay.model.dto.CreatePaymentResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pay-service", contextId = "payClient")
public interface PayClient {

    @PostMapping("/internal/payments")
    Result<CreatePaymentResultDTO> createPayment(@RequestBody CreatePaymentDTO dto);
}
