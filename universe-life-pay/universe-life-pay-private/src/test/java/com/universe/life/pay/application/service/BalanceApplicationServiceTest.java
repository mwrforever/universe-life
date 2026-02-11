package com.universe.life.pay.application.service;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.pay.domain.repository.PayBalanceAccountRepository;
import com.universe.life.pay.domain.repository.PayBalanceFlowRepository;
import com.universe.life.pay.domain.repository.PayBalanceFreezeRepository;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceAccountPO;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceFlowPO;
import com.universe.life.pay.infrastructure.persistence.po.PayBalanceFreezePO;
import com.universe.life.pay.model.enums.BalanceFlowActionEnum;
import com.universe.life.pay.model.enums.BalanceFreezeStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceApplicationServiceTest {

    @Mock
    private PayBalanceAccountRepository balanceAccountRepository;

    @Mock
    private PayBalanceFreezeRepository balanceFreezeRepository;

    @Mock
    private PayBalanceFlowRepository balanceFlowRepository;

    @InjectMocks
    private BalanceApplicationService service;

    @Test
    void freeze_whenFreezeExisted_frozen_shouldReturnFreezeNo_andNotUpdateAccount() {
        PayBalanceFreezePO existed = new PayBalanceFreezePO();
        existed.setFreezeNo("FZ-EXIST");
        existed.setAmount(100L);
        existed.setStatus(BalanceFreezeStatusEnum.FROZEN.getCode());

        when(balanceFreezeRepository.findByRequestNoAndPayerId("REQ-1", 10L)).thenReturn(Optional.of(existed));

        Result<String> result = service.freeze("REQ-1", "ORDER", 1L, 10L, 100L, null, null);

        assertNotNull(result);
        assertEquals(1, result.code());
        assertEquals("FZ-EXIST", result.data());

        verify(balanceAccountRepository, never()).freeze(anyLong(), anyString(), anyString(), anyLong(), anyLong());
        verify(balanceFreezeRepository, never()).save(any());
        verify(balanceFlowRepository, never()).save(any());
    }

    @Test
    void freeze_whenConcurrentUpdateRetry_shouldSucceed_andSaveFlowOnce() {
        when(balanceFreezeRepository.findByRequestNoAndPayerId("REQ-1", 10L)).thenReturn(Optional.empty());

        AtomicInteger findCount = new AtomicInteger(0);
        when(balanceAccountRepository.findByUserId(10L)).thenAnswer(invocation -> {
            int c = findCount.getAndIncrement();
            if (c == 0) {
                return Optional.empty();
            }
            PayBalanceAccountPO po = new PayBalanceAccountPO();
            po.setId(1L);
            po.setUserId(10L);
            po.setAccountType("DEFAULT");
            po.setCurrency("CNY");
            if (c == 1) {
                po.setAvailableBalance(200L);
                po.setFrozenBalance(0L);
                po.setVersion(1L);
            } else if (c == 2) {
                po.setAvailableBalance(200L);
                po.setFrozenBalance(0L);
                po.setVersion(2L);
            } else {
                po.setAvailableBalance(100L);
                po.setFrozenBalance(100L);
                po.setVersion(3L);
            }
            return Optional.of(po);
        });

        doAnswer(invocation -> {
            PayBalanceAccountPO po = invocation.getArgument(0);
            po.setId(1L);
            return po;
        }).when(balanceAccountRepository).save(any());

        doAnswer(invocation -> invocation.getArgument(0)).when(balanceFreezeRepository).save(any());

        when(balanceAccountRepository.freeze(eq(10L), anyString(), anyString(), eq(100L), anyLong()))
                .thenReturn(false)
                .thenReturn(true);

        when(balanceFlowRepository.existsByRequestNoUserAction("REQ-1", 10L, BalanceFlowActionEnum.FREEZE.getCode()))
                .thenReturn(false);

        doAnswer(invocation -> invocation.getArgument(0)).when(balanceFlowRepository).save(any());

        Result<String> result = service.freeze("REQ-1", "ORDER", 1L, 10L, 100L, null, "{}");

        assertNotNull(result);
        assertEquals(1, result.code());
        assertNotNull(result.data());

        verify(balanceAccountRepository, times(2)).freeze(eq(10L), anyString(), anyString(), eq(100L), anyLong());

        ArgumentCaptor<PayBalanceFlowPO> flowCaptor = ArgumentCaptor.forClass(PayBalanceFlowPO.class);
        verify(balanceFlowRepository, times(1)).save(flowCaptor.capture());

        PayBalanceFlowPO flow = flowCaptor.getValue();
        assertEquals("REQ-1", flow.getRequestNo());
        assertEquals(10L, flow.getUserId());
        assertEquals(BalanceFlowActionEnum.FREEZE.getCode(), flow.getAction());
        assertEquals(-100L, flow.getAvailableDelta());
        assertEquals(100L, flow.getFrozenDelta());
    }

    @Test
    void transfer_whenFlowIdempotent_shouldReturnSuccess_andNotUpdateAccounts() {
        when(balanceFlowRepository.existsByRequestNoUserAction("REQ-T-OUT", 1L, BalanceFlowActionEnum.TRANSFER_OUT.getCode()))
                .thenReturn(true);
        when(balanceFlowRepository.existsByRequestNoUserAction("REQ-T-IN", 2L, BalanceFlowActionEnum.TRANSFER_IN.getCode()))
                .thenReturn(true);

        Result<Void> result = service.transfer("REQ-T", "ORDER", 9L, 1L, 2L, 10L, null);

        assertNotNull(result);
        assertEquals(1, result.code());

        verify(balanceAccountRepository, never()).debit(anyLong(), anyString(), anyString(), anyLong(), anyLong());
        verify(balanceAccountRepository, never()).credit(anyLong(), anyString(), anyString(), anyLong(), anyLong());
        verify(balanceFlowRepository, never()).save(any());
    }

    @Test
    void freeze_whenFreezeSaveDuplicateKey_shouldReturnExistingFreezeNo_andNotFreezeAccount() {
        when(balanceFreezeRepository.findByRequestNoAndPayerId("REQ-1", 10L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(newFreeze("FZ-CONCURRENT", 100L, BalanceFreezeStatusEnum.FROZEN.getCode())));

        doThrow(new DuplicateKeyException("dup")).when(balanceFreezeRepository).save(any());

        Result<String> result = service.freeze("REQ-1", "ORDER", 1L, 10L, 100L, null, null);

        assertNotNull(result);
        assertEquals(1, result.code());
        assertEquals("FZ-CONCURRENT", result.data());

        verify(balanceAccountRepository, never()).freeze(anyLong(), anyString(), anyString(), anyLong(), anyLong());
        verify(balanceFlowRepository, never()).save(any());
    }

    @Test
    void freeze_whenOldFreezeClosed_newRequestNo_shouldStillFreeze() {
        PayBalanceFreezePO old = new PayBalanceFreezePO();
        old.setFreezeNo("FZ-OLD");
        old.setBizType("ORDER");
        old.setBizId(1L);
        old.setPayerId(10L);
        old.setAmount(100L);
        old.setStatus(BalanceFreezeStatusEnum.CLOSED.getCode());

        when(balanceFreezeRepository.findByRequestNoAndPayerId("REQ-NEW", 10L)).thenReturn(Optional.empty());
        when(balanceFreezeRepository.findByRequestNoAndPayerId("REQ-OLD", 10L)).thenReturn(Optional.of(old));

        PayBalanceAccountPO account = new PayBalanceAccountPO();
        account.setId(1L);
        account.setUserId(10L);
        account.setAccountType("DEFAULT");
        account.setCurrency("CNY");
        account.setAvailableBalance(200L);
        account.setFrozenBalance(0L);
        account.setVersion(1L);

        when(balanceAccountRepository.findByUserId(10L)).thenReturn(Optional.of(account));
        when(balanceAccountRepository.freeze(eq(10L), anyString(), anyString(), eq(100L), anyLong())).thenReturn(true);
        when(balanceFlowRepository.existsByRequestNoUserAction("REQ-NEW", 10L, BalanceFlowActionEnum.FREEZE.getCode())).thenReturn(true);

        Result<String> result = service.freeze("REQ-NEW", "ORDER", 1L, 10L, 100L, null, null);

        assertNotNull(result);
        assertEquals(1, result.code());
        assertNotNull(result.data());
        assertNotEquals("FZ-OLD", result.data());
    }

    private PayBalanceFreezePO newFreeze(String freezeNo, Long amount, Integer status) {
        PayBalanceFreezePO po = new PayBalanceFreezePO();
        po.setFreezeNo(freezeNo);
        po.setAmount(amount);
        po.setStatus(status);
        return po;
    }
}
