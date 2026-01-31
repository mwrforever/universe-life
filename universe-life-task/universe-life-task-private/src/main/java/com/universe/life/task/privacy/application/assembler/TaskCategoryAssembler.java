package com.universe.life.task.privacy.application.assembler;

import com.universe.life.task.privacy.application.dto.TaskCategoryDTO;
import com.universe.life.task.privacy.interfaces.vo.TaskCategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskCategoryAssembler {


    List<TaskCategoryVO> toTaskCategoryVO(List<TaskCategoryDTO> categories);
}
