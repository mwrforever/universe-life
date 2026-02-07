package com.universe.life.task.privacy.application.assembler;

import com.universe.life.task.privacy.application.dto.TaskCategoryDTO;
import com.universe.life.task.privacy.application.dto.TaskDTO;
import com.universe.life.task.privacy.domain.model.Task;
import com.universe.life.task.privacy.domain.model.TaskCategory;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskPO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

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


    // ==================== TaskCategory 映射 ====================

    /**
     * 领域模型转DTO
     */
    TaskCategoryDTO toTaskCategoryDTO(TaskCategory category);

    /**
     * DTO转领域模型
     */
    TaskCategory toTaskCategory(TaskCategoryDTO categoryDTO);
}
