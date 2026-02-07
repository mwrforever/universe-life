package com.universe.life.aftercare.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.aftercare.infrastructure.persistence.po.AftercareAppealPO;

import java.util.Optional;

public interface AftercareAppealRepository {

    AftercareAppealPO save(AftercareAppealPO po);

    Optional<AftercareAppealPO> findById(Long id);

    boolean existsPendingByOrderId(Long orderId);

    Optional<AftercareAppealPO> findLatestByOrderId(Long orderId);

    Page<AftercareAppealPO> pagePending(int pageNum, int pageSize);
}
