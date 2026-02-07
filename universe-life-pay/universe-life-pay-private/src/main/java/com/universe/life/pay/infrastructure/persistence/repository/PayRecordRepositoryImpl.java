package com.universe.life.pay.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.universe.life.pay.domain.repository.PayRecordRepository;
import com.universe.life.pay.infrastructure.persistence.mapper.PayRecordMapper;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.model.enums.PayStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PayRecordRepositoryImpl implements PayRecordRepository {

    private final PayRecordMapper mapper;

    @Override
    public PayRecordPO save(PayRecordPO po) {
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return po;
    }

    @Override
    public Optional<PayRecordPO> findByRequestNo(String requestNo) {
        if (requestNo == null || requestNo.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<PayRecordPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayRecordPO::getRequestNo, requestNo)
                .eq(PayRecordPO::getDeleted, 0)
                .last("limit 1");
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public boolean markSuccess(Long id, String thirdTradeNo, String extra) {
        if (id == null) {
            return false;
        }
        Set<Integer> allowedFrom = Set.of(
                PayStatusEnum.CREATED.getCode(),
                PayStatusEnum.PAYING.getCode(),
                PayStatusEnum.FAIL.getCode()
        );
        LambdaUpdateWrapper<PayRecordPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayRecordPO::getId, id)
                .eq(PayRecordPO::getDeleted, 0)
                .in(PayRecordPO::getStatus, allowedFrom)
                .set(PayRecordPO::getStatus, PayStatusEnum.SUCCESS.getCode())
                .set(PayRecordPO::getThirdTradeNo, thirdTradeNo)
                .set(PayRecordPO::getPaidAt, LocalDateTime.now())
                .set(PayRecordPO::getExtra, extra)
                .set(PayRecordPO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean markFail(Long id, String thirdTradeNo, String extra) {
        if (id == null) {
            return false;
        }
        Set<Integer> allowedFrom = Set.of(
                PayStatusEnum.CREATED.getCode(),
                PayStatusEnum.PAYING.getCode()
        );
        LambdaUpdateWrapper<PayRecordPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PayRecordPO::getId, id)
                .eq(PayRecordPO::getDeleted, 0)
                .in(PayRecordPO::getStatus, allowedFrom)
                .set(PayRecordPO::getStatus, PayStatusEnum.FAIL.getCode())
                .set(PayRecordPO::getThirdTradeNo, thirdTradeNo)
                .set(PayRecordPO::getExtra, extra)
                .set(PayRecordPO::getUpdatedAt, LocalDateTime.now());
        return mapper.update(null, wrapper) > 0;
    }
}
