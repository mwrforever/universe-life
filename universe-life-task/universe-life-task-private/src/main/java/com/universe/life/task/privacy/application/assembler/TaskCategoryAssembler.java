package com.universe.life.task.privacy.application.assembler;

import com.universe.life.task.privacy.domain.model.valueobject.TaskCategory;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskCategoryPO;
import com.universe.life.task.privacy.interfaces.dto.response.TaskCategoryDTO;
import com.universe.life.task.privacy.interfaces.vo.TaskCategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskCategoryAssembler {

    TaskCategoryPO toTaskCategoryPO(TaskCategory category);

    TaskCategory toTaskCategory(TaskCategoryPO categoryPO);

    TaskCategoryDTO toTaskCategoryDTO(TaskCategory category);

    TaskCategory toTaskCategory(TaskCategoryDTO categoryDTO);

    TaskCategoryDTO toTaskCategoryDTO(TaskCategoryPO categoryPO);

    TaskCategoryPO toTaskCategoryPO(TaskCategoryDTO categoryDTO);

    List<TaskCategoryVO> toTaskCategoryVO(List<TaskCategoryDTO> categories);
}
