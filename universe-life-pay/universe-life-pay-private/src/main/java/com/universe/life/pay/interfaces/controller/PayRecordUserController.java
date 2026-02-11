package com.universe.life.pay.interfaces.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.common.domain.PageResult;
import com.universe.life.pay.application.service.PayRecordUserQueryApplicationService;
import com.universe.life.pay.model.dto.UserPayRecordDTO;
import com.universe.life.pay.model.dto.UserPayRecordListQueryDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户支付记录", description = "用户端仅能查询自己的支付记录")
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayRecordUserController {

    private final PayRecordUserQueryApplicationService payRecordUserQueryApplicationService;

    @GetMapping("/records")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<UserPayRecordDTO>> myRecords(UserPayRecordListQueryDTO query) {
        Long userId = SecurityUtil.getUserId();
        return payRecordUserQueryApplicationService.pageMyRecords(userId, query);
    }

    @GetMapping("/records/{requestNo}")
    @PreAuthorize("isAuthenticated()")
    public Result<UserPayRecordDTO> myRecord(@PathVariable String requestNo) {
        Long userId = SecurityUtil.getUserId();
        return payRecordUserQueryApplicationService.getMyRecordByRequestNo(userId, requestNo);
    }
}
