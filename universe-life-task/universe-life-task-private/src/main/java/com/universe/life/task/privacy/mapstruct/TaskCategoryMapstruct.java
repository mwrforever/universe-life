package com.universe.life.task.privacy.mapstruct;

import com.universe.life.task.privacy.domain.dto.request.TaskCategoryRequest;
import com.universe.life.task.privacy.domain.po.TaskCategory;
import com.universe.life.task.privacy.domain.vo.TaskCategoryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 任务分类对象转换器
 */
@Mapper(componentModel = "spring")
public interface TaskCategoryMapstruct {

    TaskCategoryMapstruct INSTANCE = Mappers.getMapper(TaskCategoryMapstruct.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    TaskCategory toEntity(TaskCategoryRequest request);

    TaskCategoryVO toVO(TaskCategory category);

    List<TaskCategoryVO> toVOList(List<TaskCategory> categories);
}
