package com.universe.life.message.repository;

import com.universe.life.message.domain.document.AuditLogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 MongoDB Repository
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLogDocument, String> {

    /**
     * 根据用户ID查询审计日志
     */
    Page<AuditLogDocument> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据操作类型查询审计日志
     */
    Page<AuditLogDocument> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    /**
     * 根据用户ID和时间范围查询审计日志
     */
    List<AuditLogDocument> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据目标类型和目标ID查询审计日志
     */
    List<AuditLogDocument> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            String targetType, String targetId);
}
