package com.universe.life.pay.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.pay.domain.repository.PayRecordRepository;
import com.universe.life.pay.infrastructure.persistence.mapper.PayRecordMapper;
import com.universe.life.pay.infrastructure.persistence.po.PayRecordPO;
import com.universe.life.pay.model.dto.AdminPayRecordListQueryDTO;
import com.universe.life.pay.model.dto.UserPayRecordListQueryDTO;
import com.universe.life.pay.model.enums.PayStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
    public Optional<PayRecordPO> findByRequestNoAndPayerId(String requestNo, Long payerId) {
        if (!StringUtils.hasText(requestNo) || payerId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<PayRecordPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayRecordPO::getRequestNo, requestNo)
                .eq(PayRecordPO::getPayerId, payerId)
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

    @Override
    public IPage<PayRecordPO> pageAdminRecords(Page<PayRecordPO> page, AdminPayRecordListQueryDTO query) {
        if (page == null) {
            page = new Page<>(1, 20);
        }
        LambdaQueryWrapper<PayRecordPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayRecordPO::getDeleted, 0)
                .eq(query != null && StringUtils.hasText(query.getRequestNo()), PayRecordPO::getRequestNo, query.getRequestNo())
                .eq(query != null && StringUtils.hasText(query.getBizType()), PayRecordPO::getBizType, query.getBizType())
                .eq(query != null && query.getBizId() != null, PayRecordPO::getBizId, query.getBizId())
                .eq(query != null && query.getPayerId() != null, PayRecordPO::getPayerId, query.getPayerId())
                .eq(query != null && query.getPayeeId() != null, PayRecordPO::getPayeeId, query.getPayeeId())
                .eq(query != null && query.getChannel() != null, PayRecordPO::getChannel, query.getChannel())
                .eq(query != null && query.getStatus() != null, PayRecordPO::getStatus, query.getStatus())
                .ge(query != null && query.getStartTime() != null, PayRecordPO::getCreatedAt, query.getStartTime())
                .le(query != null && query.getEndTime() != null, PayRecordPO::getCreatedAt, query.getEndTime())
                .orderByDesc(PayRecordPO::getId);
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<PayRecordPO> pageUserRecords(Page<PayRecordPO> page, Long payerId, UserPayRecordListQueryDTO query) {
        if (page == null) {
            page = new Page<>(1, 20);
        }
        if (payerId == null) {
            return page;
        }
        LambdaQueryWrapper<PayRecordPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayRecordPO::getDeleted, 0)
                .eq(PayRecordPO::getPayerId, payerId)
                .eq(query != null && StringUtils.hasText(query.getBizType()), PayRecordPO::getBizType, query.getBizType())
                .eq(query != null && query.getBizId() != null, PayRecordPO::getBizId, query.getBizId())
                .eq(query != null && query.getChannel() != null, PayRecordPO::getChannel, query.getChannel())
                .eq(query != null && query.getStatus() != null, PayRecordPO::getStatus, query.getStatus())
                .ge(query != null && query.getStartTime() != null, PayRecordPO::getCreatedAt, query.getStartTime())
                .le(query != null && query.getEndTime() != null, PayRecordPO::getCreatedAt, query.getEndTime())
                .orderByDesc(PayRecordPO::getId);
        return mapper.selectPage(page, wrapper);
    }
}
