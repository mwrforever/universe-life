package com.universe.life.task.privacy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.universe.life.task.privacy.infrastructure.enums.TaskReviewStatus;
import com.universe.life.task.privacy.domain.repository.TaskReviewRecordRepository;
import com.universe.life.task.privacy.infrastructure.persistence.mapper.TaskReviewRecordMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskReviewRecordPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TaskReviewRecord 仓储实现
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Repository
@RequiredArgsConstructor
public class TaskReviewRecordRepositoryImpl implements TaskReviewRecordRepository {

    private final TaskReviewRecordMapper reviewRecordMapper;

    @Override
    public void createRecord(Long taskId, Long reviewerId, TaskReviewStatus status,
                             String rejectReason, LocalDateTime reviewedAt) {
        TaskReviewRecordPO po = TaskReviewRecordPO.builder()
                .taskId(taskId)
                .reviewerId(reviewerId)
                .status(status)
                .rejectReason(rejectReason)
                .reviewedAt(reviewedAt)
                .createdAt(LocalDateTime.now())
                .build();
        reviewRecordMapper.insert(po);
    }

    @Override
    public List<TaskReviewRecordPO> findByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskReviewRecordPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskReviewRecordPO::getTaskId, taskId)
                .orderByDesc(TaskReviewRecordPO::getCreatedAt);
        return reviewRecordMapper.selectList(wrapper);
    }
}
