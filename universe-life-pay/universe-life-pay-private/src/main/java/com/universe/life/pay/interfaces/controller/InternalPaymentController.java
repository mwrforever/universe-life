package com.universe.life.pay.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.pay.application.service.PayApplicationService;
import com.universe.life.pay.model.dto.CreatePaymentDTO;
import com.universe.life.pay.model.dto.CreatePaymentResultDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "内部支付接口", description = "供其他服务调用的内部支付接口")
@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PayApplicationService payApplicationService;

    @PostMapping
    public Result<CreatePaymentResultDTO> create(@RequestBody CreatePaymentDTO dto) {
        return payApplicationService.createPayment(dto);
    }
}
