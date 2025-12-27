package com.universe.life.task.privacy.mapstruct;

import com.universe.life.task.privacy.domain.dto.request.TaskCreateRequest;
import com.universe.life.task.privacy.domain.po.Task;
import com.universe.life.task.privacy.domain.vo.TaskDetailVO;
import com.universe.life.task.privacy.domain.vo.TaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 任务对象转换器
 */
@Mapper(componentModel = "spring")
public interface TaskMapstruct {

    TaskMapstruct INSTANCE = Mappers.getMapper(TaskMapstruct.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publisherId", ignore = true)
    @Mapping(target = "depositAmount", ignore = true)
    @Mapping(target = "depositStatus", ignore = true)
    @Mapping(target = "currentAcceptors", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reviewStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    Task toEntity(TaskCreateRequest request);

    @Mapping(target = "publisherName", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    TaskVO toVO(Task task);

    @Mapping(target = "publisherName", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "reviewRecord", ignore = true)
    TaskDetailVO toDetailVO(Task task);

    List<TaskVO> toVOList(List<Task> tasks);
}
