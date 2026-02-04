package com.universe.life.task.privacy.domain.repository;

import com.universe.life.task.privacy.infrastructure.enums.TaskReviewStatus;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskReviewRecordPO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TaskReviewRecord 仓储接口
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
public interface TaskReviewRecordRepository {

    /**
     * 创建审核记录
     */
    void createRecord(Long taskId, Long reviewerId, TaskReviewStatus status, 
                      String rejectReason, LocalDateTime reviewedAt);

    /**
     * 根据任务ID查询审核记录
     */
    List<TaskReviewRecordPO> findByTaskId(Long taskId);
}
