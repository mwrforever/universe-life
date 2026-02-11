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
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BalanceApplicationService {

    private static final String DEFAULT_ACCOUNT_TYPE = "DEFAULT";
    private static final String DEFAULT_CURRENCY = "CNY";

    private static final int MAX_RETRY = 3;

    private final PayBalanceAccountRepository balanceAccountRepository;
    private final PayBalanceFreezeRepository balanceFreezeRepository;
    private final PayBalanceFlowRepository balanceFlowRepository;

    @Transactional(rollbackFor = Exception.class)
    public Result<String> freeze(String requestNo, String bizType, Long bizId, Long payerId, Long amount, LocalDateTime expireAt, String extra) {
        if (requestNo == null || requestNo.isBlank()) {
            return Result.error("requestNo不能为空");
        }
        if (bizType == null || bizType.isBlank()) {
            return Result.error("bizType不能为空");
        }
        if (bizId == null) {
            return Result.error("bizId不能为空");
        }
        if (payerId == null) {
            return Result.error("payerId不能为空");
        }
        if (amount == null || amount <= 0) {
            return Result.error("amount非法");
        }

        PayBalanceFreezePO existed = balanceFreezeRepository.findByRequestNoAndPayerId(requestNo, payerId).orElse(null);
        if (existed != null) {
            if (existed.getBizType() != null && !bizType.equals(existed.getBizType())) {
                return Result.error("bizType不一致");
            }
            if (existed.getBizId() != null && !bizId.equals(existed.getBizId())) {
                return Result.error("bizId不一致");
            }
            if (!amount.equals(existed.getAmount())) {
                return Result.error("冻结金额不一致");
            }
            if (existed.getStatus() != null && existed.getStatus() == BalanceFreezeStatusEnum.FROZEN.getCode()) {
                return Result.success(existed.getFreezeNo());
            }
            if (existed.getStatus() != null && existed.getStatus() == BalanceFreezeStatusEnum.CONFIRMED_DEBIT.getCode()) {
                return Result.success(existed.getFreezeNo());
            }
            if (existed.getStatus() != null && existed.getStatus() == BalanceFreezeStatusEnum.UNFROZEN.getCode()) {
                return Result.error("冻结已解冻");
            }
            return Result.error("冻结已关闭");
        }

        ensureAccount(payerId);

        String freezeNo = genNo("FZ");
        LocalDateTime now = LocalDateTime.now();

        PayBalanceFreezePO freeze = new PayBalanceFreezePO();
        freeze.setFreezeNo(freezeNo);
        freeze.setBizType(bizType);
        freeze.setBizId(bizId);
        freeze.setRequestNo(requestNo);
        freeze.setPayerId(payerId);
        freeze.setAmount(amount);
        freeze.setStatus(BalanceFreezeStatusEnum.FROZEN.getCode());
        freeze.setExpireAt(expireAt);
        freeze.setExtra(extra);
        freeze.setCreatedAt(now);
        freeze.setUpdatedAt(now);

        try {
            balanceFreezeRepository.save(freeze);
        } catch (DuplicateKeyException e) {
            PayBalanceFreezePO concurrent = balanceFreezeRepository.findByRequestNoAndPayerId(requestNo, payerId).orElse(null);
            if (concurrent != null) {
                return Result.success(concurrent.getFreezeNo());
            }
            throw e;
        }

        boolean frozenOk = false;
        PayBalanceAccountPO after = null;
        for (int i = 0; i < MAX_RETRY; i++) {
            PayBalanceAccountPO account = getAccountRequired(payerId);
            if (account.getAvailableBalance() == null || account.getAvailableBalance() < amount) {
                return Result.error("余额不足");
            }
            boolean updated = balanceAccountRepository.freeze(payerId, DEFAULT_ACCOUNT_TYPE, DEFAULT_CURRENCY, amount, account.getVersion());
            if (updated) {
                frozenOk = true;
                after = getAccountRequired(payerId);
                break;
            }
        }

        if (!frozenOk) {
            return Result.error("余额并发冲突，请重试");
        }

        if (!balanceFlowRepository.existsByRequestNoUserAction(requestNo, payerId, BalanceFlowActionEnum.FREEZE.getCode())) {
            PayBalanceFlowPO flow = newFlow(requestNo, payerId, bizType, bizId,
                    BalanceFlowActionEnum.FREEZE.getCode(), amount,
                    -amount, amount,
                    after.getAvailableBalance(), after.getFrozenBalance(),
                    null, extra);
            balanceFlowRepository.save(flow);
        }

        return Result.success(freezeNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> unfreeze(String requestNo, String bizType, Long bizId, Long payerId, String extra) {
        if (requestNo == null || requestNo.isBlank()) {
            return Result.error("requestNo不能为空");
        }
        if (bizType == null || bizType.isBlank()) {
            return Result.error("bizType不能为空");
        }
        if (bizId == null) {
            return Result.error("bizId不能为空");
        }
        if (payerId == null) {
            return Result.error("payerId不能为空");
        }

        PayBalanceFreezePO freeze = balanceFreezeRepository.findByRequestNoAndPayerId(requestNo, payerId).orElse(null);
        if (freeze == null) {
            return Result.error("冻结记录不存在");
        }
        if (freeze.getBizType() != null && !bizType.equals(freeze.getBizType())) {
            return Result.error("bizType不一致");
        }
        if (freeze.getBizId() != null && !bizId.equals(freeze.getBizId())) {
            return Result.error("bizId不一致");
        }
        if (freeze.getStatus() != null && freeze.getStatus() == BalanceFreezeStatusEnum.UNFROZEN.getCode()) {
            return Result.success();
        }
        if (freeze.getStatus() != null && freeze.getStatus() == BalanceFreezeStatusEnum.CONFIRMED_DEBIT.getCode()) {
            return Result.error("冻结已扣款确认");
        }
        if (freeze.getStatus() != null && freeze.getStatus() != BalanceFreezeStatusEnum.FROZEN.getCode()) {
            return Result.error("冻结状态非法");
        }

        Long amount = freeze.getAmount();
        if (amount == null || amount <= 0) {
            return Result.error("冻结金额非法");
        }

        boolean ok = false;
        PayBalanceAccountPO after = null;
        for (int i = 0; i < MAX_RETRY; i++) {
            PayBalanceAccountPO account = getAccountRequired(payerId);
            if (account.getFrozenBalance() == null || account.getFrozenBalance() < amount) {
                return Result.error("冻结余额不足");
            }
            boolean updated = balanceAccountRepository.unfreeze(payerId, DEFAULT_ACCOUNT_TYPE, DEFAULT_CURRENCY, amount, account.getVersion());
            if (updated) {
                ok = true;
                after = getAccountRequired(payerId);
                break;
            }
        }

        if (!ok) {
            return Result.error("余额并发冲突，请重试");
        }

        balanceFreezeRepository.markUnfrozen(freeze.getId());

        if (!balanceFlowRepository.existsByRequestNoUserAction(requestNo, payerId, BalanceFlowActionEnum.UNFREEZE.getCode())) {
            PayBalanceFlowPO flow = newFlow(requestNo, payerId, bizType, bizId,
                    BalanceFlowActionEnum.UNFREEZE.getCode(), amount,
                    amount, -amount,
                    after.getAvailableBalance(), after.getFrozenBalance(),
                    null, extra);
            balanceFlowRepository.save(flow);
        }

        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> confirmDebit(String requestNo, String bizType, Long bizId, Long payerId, String extra) {
        if (requestNo == null || requestNo.isBlank()) {
            return Result.error("requestNo不能为空");
        }
        if (bizType == null || bizType.isBlank()) {
            return Result.error("bizType不能为空");
        }
        if (bizId == null) {
            return Result.error("bizId不能为空");
        }
        if (payerId == null) {
            return Result.error("payerId不能为空");
        }

        PayBalanceFreezePO freeze = balanceFreezeRepository.findByRequestNoAndPayerId(requestNo, payerId).orElse(null);
        if (freeze == null) {
            return Result.error("冻结记录不存在");
        }
        if (freeze.getBizType() != null && !bizType.equals(freeze.getBizType())) {
            return Result.error("bizType不一致");
        }
        if (freeze.getBizId() != null && !bizId.equals(freeze.getBizId())) {
            return Result.error("bizId不一致");
        }
        if (freeze.getStatus() != null && freeze.getStatus() == BalanceFreezeStatusEnum.CONFIRMED_DEBIT.getCode()) {
            return Result.success();
        }
        if (freeze.getStatus() == null || freeze.getStatus() != BalanceFreezeStatusEnum.FROZEN.getCode()) {
            return Result.error("冻结状态非法");
        }

        Long amount = freeze.getAmount();
        if (amount == null || amount <= 0) {
            return Result.error("冻结金额非法");
        }

        boolean ok = false;
        PayBalanceAccountPO after = null;
        for (int i = 0; i < MAX_RETRY; i++) {
            PayBalanceAccountPO account = getAccountRequired(payerId);
            if (account.getFrozenBalance() == null || account.getFrozenBalance() < amount) {
                return Result.error("冻结余额不足");
            }
            boolean updated = balanceAccountRepository.confirmDebit(payerId, DEFAULT_ACCOUNT_TYPE, DEFAULT_CURRENCY, amount, account.getVersion());
            if (updated) {
                ok = true;
                after = getAccountRequired(payerId);
                break;
            }
        }

        if (!ok) {
            return Result.error("余额并发冲突，请重试");
        }

        balanceFreezeRepository.markConfirmedDebit(freeze.getId());

        if (!balanceFlowRepository.existsByRequestNoUserAction(requestNo, payerId, BalanceFlowActionEnum.CONFIRM_DEBIT.getCode())) {
            PayBalanceFlowPO flow = newFlow(requestNo, payerId, bizType, bizId,
                    BalanceFlowActionEnum.CONFIRM_DEBIT.getCode(), amount,
                    0L, -amount,
                    after.getAvailableBalance(), after.getFrozenBalance(),
                    null, extra);
            balanceFlowRepository.save(flow);
        }

        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> transfer(String requestNo, String bizType, Long bizId, Long fromUserId, Long toUserId, Long amount, String extra) {
        if (bizType == null || bizType.isBlank()) {
            return Result.error("bizType不能为空");
        }
        if (bizId == null) {
            return Result.error("bizId不能为空");
        }
        if (fromUserId == null || toUserId == null) {
            return Result.error("用户ID不能为空");
        }
        if (fromUserId.equals(toUserId)) {
            return Result.error("转出转入不能为同一用户");
        }
        if (amount == null || amount <= 0) {
            return Result.error("amount非法");
        }

        ensureAccount(fromUserId);
        ensureAccount(toUserId);

        String outReq = requestNo == null ? "" : requestNo + "-OUT";
        String inReq = requestNo == null ? "" : requestNo + "-IN";

        if (requestNo != null && !requestNo.isBlank()) {
            if (balanceFlowRepository.existsByRequestNoUserAction(outReq, fromUserId, BalanceFlowActionEnum.TRANSFER_OUT.getCode())
                    && balanceFlowRepository.existsByRequestNoUserAction(inReq, toUserId, BalanceFlowActionEnum.TRANSFER_IN.getCode())) {
                return Result.success();
            }
        }

        PayBalanceAccountPO fromAfter;
        boolean outOk = false;
        for (int i = 0; i < MAX_RETRY; i++) {
            PayBalanceAccountPO from = getAccountRequired(fromUserId);
            if (from.getAvailableBalance() == null || from.getAvailableBalance() < amount) {
                return Result.error("余额不足");
            }
            boolean updated = balanceAccountRepository.debit(fromUserId, DEFAULT_ACCOUNT_TYPE, DEFAULT_CURRENCY, amount, from.getVersion());
            if (updated) {
                outOk = true;
                break;
            }
        }
        if (!outOk) {
            return Result.error("余额并发冲突，请重试");
        }
        fromAfter = getAccountRequired(fromUserId);

        PayBalanceAccountPO toAfter;
        boolean inOk = false;
        for (int i = 0; i < MAX_RETRY; i++) {
            PayBalanceAccountPO to = getAccountRequired(toUserId);
            boolean updated = balanceAccountRepository.credit(toUserId, DEFAULT_ACCOUNT_TYPE, DEFAULT_CURRENCY, amount, to.getVersion());
            if (updated) {
                inOk = true;
                break;
            }
        }
        if (!inOk) {
            return Result.error("余额并发冲突，请重试");
        }
        toAfter = getAccountRequired(toUserId);

        if (requestNo != null && !requestNo.isBlank()) {
            if (!balanceFlowRepository.existsByRequestNoUserAction(outReq, fromUserId, BalanceFlowActionEnum.TRANSFER_OUT.getCode())) {
                PayBalanceFlowPO outFlow = newFlow(outReq, fromUserId, bizType, bizId,
                        BalanceFlowActionEnum.TRANSFER_OUT.getCode(), amount,
                        -amount, 0L,
                        fromAfter.getAvailableBalance(), fromAfter.getFrozenBalance(),
                        null, extra);
                balanceFlowRepository.save(outFlow);
            }
            if (!balanceFlowRepository.existsByRequestNoUserAction(inReq, toUserId, BalanceFlowActionEnum.TRANSFER_IN.getCode())) {
                PayBalanceFlowPO inFlow = newFlow(inReq, toUserId, bizType, bizId,
                        BalanceFlowActionEnum.TRANSFER_IN.getCode(), amount,
                        amount, 0L,
                        toAfter.getAvailableBalance(), toAfter.getFrozenBalance(),
                        null, extra);
                balanceFlowRepository.save(inFlow);
            }
        }

        return Result.success();
    }

    @Transactional(readOnly = true)
    public Result<PayBalanceAccountPO> query(Long userId) {
        if (userId == null) {
            return Result.error("userId不能为空");
        }
        PayBalanceAccountPO account = balanceAccountRepository.findByUserId(userId).orElse(null);
        if (account == null) {
            return Result.error("账户不存在");
        }
        return Result.success(account);
    }

    private void ensureAccount(Long userId) {
        Optional<PayBalanceAccountPO> existed = balanceAccountRepository.findByUserId(userId);
        if (existed.isPresent()) {
            return;
        }
        PayBalanceAccountPO po = new PayBalanceAccountPO();
        po.setUserId(userId);
        po.setAccountType(DEFAULT_ACCOUNT_TYPE);
        po.setCurrency(DEFAULT_CURRENCY);
        po.setAvailableBalance(0L);
        po.setFrozenBalance(0L);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        try {
            balanceAccountRepository.save(po);
        } catch (DuplicateKeyException e) {
            // ignore
        }
    }

    private PayBalanceAccountPO getAccountRequired(Long userId) {
        return balanceAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("余额账户不存在"));
    }

    private PayBalanceFlowPO newFlow(String requestNo, Long userId, String bizType, Long bizId,
                                     String action, Long amount,
                                     Long availableDelta, Long frozenDelta,
                                     Long balanceAfter, Long frozenAfter,
                                     String remark, String extra) {
        PayBalanceFlowPO flow = new PayBalanceFlowPO();
        flow.setFlowNo(genNo("FL"));
        flow.setRequestNo(requestNo);
        flow.setUserId(userId);
        flow.setBizType(bizType);
        flow.setBizId(bizId);
        flow.setAction(action);
        flow.setAmount(amount);
        flow.setAvailableDelta(availableDelta);
        flow.setFrozenDelta(frozenDelta);
        flow.setBalanceAfter(balanceAfter);
        flow.setFrozenAfter(frozenAfter);
        flow.setRemark(remark);
        flow.setExtra(extra);
        flow.setCreatedAt(LocalDateTime.now());
        flow.setUpdatedAt(LocalDateTime.now());
        return flow;
    }

    private String genNo(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }
}
