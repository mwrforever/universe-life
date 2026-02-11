package com.universe.life.pay.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.common.domain.PageResult;
import com.universe.life.pay.application.service.PayRecordQueryApplicationService;
import com.universe.life.pay.model.dto.AdminPayRecordDTO;
import com.universe.life.pay.model.dto.AdminPayRecordListQueryDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端支付记录", description = "管理端支付记录查询")
@RestController
@RequestMapping("/admin/pay")
@RequiredArgsConstructor
public class AdminPayRecordController {

    private final PayRecordQueryApplicationService payRecordQueryApplicationService;

    @GetMapping("/records")
    @PreAuthorize("@pm.match('pay:admin:pay:records')")
    public Result<PageResult<AdminPayRecordDTO>> records(AdminPayRecordListQueryDTO query) {
        return payRecordQueryApplicationService.pageAdminRecords(query);
    }
}
