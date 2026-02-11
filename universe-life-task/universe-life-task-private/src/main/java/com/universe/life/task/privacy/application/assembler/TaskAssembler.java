package com.universe.life.task.privacy.application.assembler;

import com.universe.life.task.privacy.application.command.CancelTaskCommand;
import com.universe.life.task.privacy.application.command.CreateTaskCommand;
import com.universe.life.task.privacy.application.command.UpdateTaskCommand;
import com.universe.life.task.privacy.application.query.TaskHallQuery;
import com.universe.life.task.privacy.application.assembler.support.TaskFormatSupport;
import com.universe.life.task.privacy.interfaces.dto.request.TaskCreateRequest;
import com.universe.life.task.privacy.interfaces.dto.request.TaskHallQueryRequest;
import com.universe.life.task.privacy.interfaces.dto.request.TaskUpdateRequest;
import com.universe.life.task.privacy.interfaces.dto.response.TaskDTO;
import com.universe.life.task.privacy.domain.model.aggregate.Task;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskPO;
import com.universe.life.task.privacy.interfaces.vo.TaskFullDetailVO;
import com.universe.life.task.privacy.interfaces.vo.TaskHallSummaryVO;
import com.universe.life.task.privacy.interfaces.vo.TaskPublishedSummaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Task MapStruct 映射器
 * 负责 Task 领域模型、PO、DTO、VO 之间的对象转换
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = TaskFormatSupport.class)
public interface TaskAssembler {

    // ==================== Request -> Command/Query ====================

    CreateTaskCommand toCreateCommand(TaskCreateRequest request);

    UpdateTaskCommand toUpdateCommand(TaskUpdateRequest request);

    TaskHallQuery toHallQuery(TaskHallQueryRequest request);

    default CancelTaskCommand toCancelCommand(Long taskId, Long userId, String reason) {
        return CancelTaskCommand.builder()
                .taskId(taskId)
                .operatorId(userId)
                .reason(reason)
                .build();
    }

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

    // ==================== DTO -> VO ====================

    @Mapping(target = "rewardAmountYuan", expression = "java(TaskFormatSupport.formatAmountToYuan(taskDTO.getRewardAmount()))")
    @Mapping(target = "depositAmountYuan", expression = "java(TaskFormatSupport.formatAmountToYuan(taskDTO.getDepositAmount()))")
    @Mapping(target = "depositStatus", expression = "java(taskDTO.getDepositStatus() != null ? taskDTO.getDepositStatus().getCode() : null)")
    @Mapping(target = "depositStatusText", expression = "java(taskDTO.getDepositStatus() != null ? taskDTO.getDepositStatus().getText() : null)")
    @Mapping(target = "status", expression = "java(taskDTO.getStatus() != null ? taskDTO.getStatus().getCode() : null)")
    @Mapping(target = "statusText", expression = "java(taskDTO.getStatus() != null ? taskDTO.getStatus().getText() : null)")
    @Mapping(target = "reviewStatus", expression = "java(taskDTO.getReviewStatus() != null ? taskDTO.getReviewStatus().getCode() : null)")
    @Mapping(target = "reviewStatusText", expression = "java(taskDTO.getReviewStatus() != null ? taskDTO.getReviewStatus().getText() : null)")
    @Mapping(target = "deadlineText", expression = "java(TaskFormatSupport.formatFriendlyTime(taskDTO.getDeadline()))")
    @Mapping(target = "publisher", ignore = true)
    @Mapping(target = "reviewRecord", ignore = true)
    @Mapping(target = "isOwner", ignore = true)
    @Mapping(target = "canApply", ignore = true)
    @Mapping(target = "hasApplied", ignore = true)
    @Mapping(target = "availableActions", ignore = true)
    TaskFullDetailVO toTaskFullDetailVO(TaskDTO taskDTO);

    @Mapping(target = "rewardAmountYuan", expression = "java(TaskFormatSupport.formatAmountToYuan(taskDTO.getRewardAmount()))")
    @Mapping(target = "deadlineText", expression = "java(TaskFormatSupport.formatFriendlyTime(taskDTO.getDeadline()))")
    @Mapping(target = "acceptorProgress", expression = "java(TaskFormatSupport.formatAcceptorProgress(taskDTO.getCurrentAcceptors(), taskDTO.getMaxAcceptors()))")
    TaskHallSummaryVO toTaskHallSummaryVO(TaskDTO taskDTO);

    @Mapping(target = "rewardAmountYuan", expression = "java(TaskFormatSupport.formatAmountToYuan(taskDTO.getRewardAmount()))")
    @Mapping(target = "status", expression = "java(taskDTO.getStatus() != null ? taskDTO.getStatus().getCode() : null)")
    @Mapping(target = "statusText", expression = "java(taskDTO.getStatus() != null ? taskDTO.getStatus().getText() : null)")
    @Mapping(target = "pendingApplicants", ignore = true)
    TaskPublishedSummaryVO toTaskPublishedSummaryVO(TaskDTO taskDTO);
}
