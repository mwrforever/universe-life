package com.universe.life.pay.application.service.channel.impl;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.pay.application.service.BalanceApplicationService;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalancePayChannelHandlerTest {

    @Mock
    private BalanceApplicationService balanceApplicationService;

    @InjectMocks
    private BalancePayChannelHandler handler;

    @Test
    void create_whenFreezeFailed_shouldThrowIllegalStateException() {
        PayRecordPO record = new PayRecordPO();
        record.setRequestNo("REQ-1");
        record.setBizType("ORDER");
        record.setBizId(1L);
        record.setPayerId(10L);
        record.setAmount(100L);

        when(balanceApplicationService.freeze(eq("REQ-1"), eq("ORDER"), eq(1L), eq(10L), eq(100L), isNull(), any()))
                .thenReturn(Result.error("余额不足"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> handler.create(record));
        assertTrue(ex.getMessage().contains("余额不足"));

        verify(balanceApplicationService, never()).confirmDebit(any(), any(), any(), any(), any());
    }

    @Test
    void create_whenConfirmFailed_shouldThrowIllegalStateException() {
        PayRecordPO record = new PayRecordPO();
        record.setRequestNo("REQ-1");
        record.setBizType("ORDER");
        record.setBizId(1L);
        record.setPayerId(10L);
        record.setAmount(100L);

        when(balanceApplicationService.freeze(eq("REQ-1"), eq("ORDER"), eq(1L), eq(10L), eq(100L), isNull(), any()))
                .thenReturn(Result.success("FZ-1"));
        when(balanceApplicationService.confirmDebit(eq("REQ-1"), eq("ORDER"), eq(1L), eq(10L), any()))
                .thenReturn(Result.error("冻结记录不存在"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> handler.create(record));
        assertTrue(ex.getMessage().contains("冻结记录不存在"));
    }

    @Test
    void create_whenSuccess_shouldReturnThirdTradeNo() {
        PayRecordPO record = new PayRecordPO();
        record.setRequestNo("REQ-1");
        record.setBizType("ORDER");
        record.setBizId(1L);
        record.setPayerId(10L);
        record.setAmount(100L);

        when(balanceApplicationService.freeze(eq("REQ-1"), eq("ORDER"), eq(1L), eq(10L), eq(100L), isNull(), any()))
                .thenReturn(Result.success("FZ-1"));
        when(balanceApplicationService.confirmDebit(eq("REQ-1"), eq("ORDER"), eq(1L), eq(10L), any()))
                .thenReturn(Result.success());

        var result = handler.create(record);
        assertNotNull(result);
        assertEquals("BAL-REQ-1", result.thirdTradeNo());
    }
}
