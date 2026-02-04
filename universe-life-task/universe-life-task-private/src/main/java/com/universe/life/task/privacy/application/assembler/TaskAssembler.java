package com.universe.life.task.privacy.application.assembler;

import com.universe.life.task.privacy.application.dto.TaskCategoryDTO;
import com.universe.life.task.privacy.application.dto.TaskDTO;
import com.universe.life.task.privacy.domain.model.Task;
import com.universe.life.task.privacy.domain.model.TaskCategory;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskPO;
import com.universe.life.task.privacy.interfaces.vo.TaskFullDetailVO;
import com.universe.life.task.privacy.interfaces.vo.TaskHallSummaryVO;
import com.universe.life.task.privacy.interfaces.vo.TaskPublishedSummaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Task MapStruct 映射器
 * 负责 Task 领域模型、PO、DTO、VO 之间的对象转换
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskAssembler {

    // ==================== Domain Model <-> PO ====================

    /**
     * 领域模型转PO
     */
    TaskPO toTaskPO(Task task);

    /**
     * PO转领域模型
     */
    Task toTask(TaskPO taskPO);


    /**
     * 领域模型转DTO
     */
    TaskDTO toTaskDTO(Task task);

    /**
     * DTO转领域模型
     */
    Task toTask(TaskDTO taskDTO);

    // ==================== PO <-> DTO ====================

    /**
     * PO转DTO
     */
    TaskDTO toTaskDTO(TaskPO taskPO);

    /**
     * DTO转PO
     */
    TaskPO toTaskPO(TaskDTO taskDTO);


    /**
     * DTO转任务详情VO
     * 包含金额格式化、时间友好文本、枚举文本
     */
    @Mapping(target = "rewardAmountYuan", expression = "java(formatAmountToYuan(taskDTO.getRewardAmount()))")
    @Mapping(target = "depositAmountYuan", expression = "java(formatAmountToYuan(taskDTO.getDepositAmount()))")
    @Mapping(target = "depositStatus", expression = "java(taskDTO.getDepositStatus() != null ? taskDTO.getDepositStatus().getCode() : null)")
    @Mapping(target = "depositStatusText", expression = "java(taskDTO.getDepositStatus() != null ? taskDTO.getDepositStatus().getText() : null)")
    @Mapping(target = "status", expression = "java(taskDTO.getStatus() != null ? taskDTO.getStatus().getCode() : null)")
    @Mapping(target = "statusText", expression = "java(taskDTO.getStatus() != null ? taskDTO.getStatus().getText() : null)")
    @Mapping(target = "reviewStatus", expression = "java(taskDTO.getReviewStatus() != null ? taskDTO.getReviewStatus().getCode() : null)")
    @Mapping(target = "reviewStatusText", expression = "java(taskDTO.getReviewStatus() != null ? taskDTO.getReviewStatus().getText() : null)")
    @Mapping(target = "deadlineText", expression = "java(formatFriendlyTime(taskDTO.getDeadline()))")
    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "reviewRecord", ignore = true)
    @Mapping(target = "isOwner", ignore = true)
    @Mapping(target = "canApply", ignore = true)
    @Mapping(target = "hasApplied", ignore = true)
    @Mapping(target = "availableActions", ignore = true)
    TaskFullDetailVO toTaskFullDetailVO(TaskDTO taskDTO);

    /**
     * DTO转任务大厅摘要VO
     */
    @Mapping(target = "rewardAmountYuan", expression = "java(formatAmountToYuan(taskDTO.getRewardAmount()))")
    @Mapping(target = "deadlineText", expression = "java(formatFriendlyTime(taskDTO.getDeadline()))")
    @Mapping(target = "acceptorProgress", expression = "java(formatAcceptorProgress(taskDTO.getCurrentAcceptors(), taskDTO.getMaxAcceptors()))")
    TaskHallSummaryVO toTaskHallSummaryVO(TaskDTO taskDTO);

    /**
     * DTO转我发布的任务摘要VO
     */
    @Mapping(target = "rewardAmountYuan", expression = "java(formatAmountToYuan(taskDTO.getRewardAmount()))")
    @Mapping(target = "status", expression = "java(taskDTO.getStatus() != null ? taskDTO.getStatus().getCode() : null)")
    @Mapping(target = "statusText", expression = "java(taskDTO.getStatus() != null ? taskDTO.getStatus().getText() : null)")
    @Mapping(target = "pendingApplicants", ignore = true)
    TaskPublishedSummaryVO toTaskPublishedSummaryVO(TaskDTO taskDTO);

    // ==================== TaskCategory 映射 ====================

    /**
     * 领域模型转DTO
     */
    TaskCategoryDTO toTaskCategoryDTO(TaskCategory category);

    /**
     * DTO转领域模型
     */
    TaskCategory toTaskCategory(TaskCategoryDTO categoryDTO);

    // ==================== 辅助方法 ====================

    /**
     * 格式化金额：分转元（保留2位小数）
     */
    default String formatAmountToYuan(Long amountInCents) {
        if (amountInCents == null) {
            return "0.00";
        }
        BigDecimal yuan = BigDecimal.valueOf(amountInCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return yuan.toString();
    }

    /**
     * 格式化友好时间文本
     */
    default String formatFriendlyTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(now, dateTime);
        long hours = ChronoUnit.HOURS.between(now, dateTime);
        long minutes = ChronoUnit.MINUTES.between(now, dateTime);

        if (days > 0) {
            return days + "天后";
        } else if (hours > 0) {
            return hours + "小时后";
        } else if (minutes > 0) {
            return minutes + "分钟后";
        } else if (minutes < 0) {
            long pastDays = Math.abs(ChronoUnit.DAYS.between(dateTime, now));
            if (pastDays > 0) {
                return pastDays + "天前";
            }
            long pastHours = Math.abs(ChronoUnit.HOURS.between(dateTime, now));
            if (pastHours > 0) {
                return pastHours + "小时前";
            }
            long pastMinutes = Math.abs(ChronoUnit.MINUTES.between(dateTime, now));
            return pastMinutes + "分钟前";
        } else {
            return "刚刚";
        }
    }

    /**
     * 格式化接单进度
     */
    default String formatAcceptorProgress(Integer current, Integer max) {
        if (current == null || max == null) {
            return "0/0";
        }
        return current + "/" + max;
    }
}
