package com.universe.life.aftercare.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.aftercare.domain.repository.AftercareAppealRepository;
import com.universe.life.aftercare.infrastructure.persistence.mapper.AftercareAppealPOMapper;
import com.universe.life.aftercare.infrastructure.persistence.po.AftercareAppealPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AftercareAppealRepositoryImpl implements AftercareAppealRepository {

    private final AftercareAppealPOMapper mapper;

    @Override
    public AftercareAppealPO save(AftercareAppealPO po) {
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return po;
    }

    @Override
    public Optional<AftercareAppealPO> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public boolean existsPendingByOrderId(Long orderId) {
        LambdaQueryWrapper<AftercareAppealPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AftercareAppealPO::getOrderId, orderId)
                .eq(AftercareAppealPO::getStatus, 0)
                .eq(AftercareAppealPO::getDeleted, 0);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public Optional<AftercareAppealPO> findLatestByOrderId(Long orderId) {
        LambdaQueryWrapper<AftercareAppealPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AftercareAppealPO::getOrderId, orderId)
                .eq(AftercareAppealPO::getDeleted, 0)
                .orderByDesc(AftercareAppealPO::getCreatedAt)
                .last("LIMIT 1");
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public Page<AftercareAppealPO> pagePending(int pageNum, int pageSize) {
        LambdaQueryWrapper<AftercareAppealPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AftercareAppealPO::getStatus, 0)
                .eq(AftercareAppealPO::getDeleted, 0)
                .orderByAsc(AftercareAppealPO::getCreatedAt);
        Page<AftercareAppealPO> page = new Page<>(pageNum, pageSize);
        return mapper.selectPage(page, wrapper);
    }
}
