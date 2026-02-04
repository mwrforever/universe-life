package com.universe.life.task.privacy.interfaces.assembler;

import com.universe.life.task.privacy.application.command.CancelTaskCommand;
import com.universe.life.task.privacy.application.command.CreateTaskCommand;
import com.universe.life.task.privacy.application.command.UpdateTaskCommand;
import com.universe.life.task.privacy.application.query.TaskHallQuery;
import com.universe.life.task.privacy.interfaces.dto.request.TaskCreateRequest;
import com.universe.life.task.privacy.interfaces.dto.request.TaskHallQueryRequest;
import com.universe.life.task.privacy.interfaces.dto.request.TaskUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 请求对象转换器
 * <p>
 * 负责将接口层的 Request 对象转换为应用层的 Command/Query 对象。
 * 遵循 DDD 分层架构规范，Controller 层负责 Request 到 Command 的转换。
 * </p>
 *
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Mapper(componentModel = "spring")
public interface TaskRequestAssembler {

    TaskRequestAssembler INSTANCE = Mappers.getMapper(TaskRequestAssembler.class);

    /**
     * Request -> CreateTaskCommand
     *
     * @param request 创建任务请求
     * @return 创建任务命令
     */
    CreateTaskCommand toCreateCommand(TaskCreateRequest request);

    /**
     * Request -> UpdateTaskCommand
     *
     * @param request 更新任务请求
     * @return 更新任务命令
     */
    UpdateTaskCommand toUpdateCommand(TaskUpdateRequest request);

    /**
     * Request -> TaskHallQuery
     *
     * @param request 任务大厅查询请求
     * @return 任务大厅查询对象
     */
    TaskHallQuery toHallQuery(TaskHallQueryRequest request);

    /**
     * 构建取消任务命令
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 取消任务命令
     */
    default CancelTaskCommand toCancelCommand(Long taskId, Long userId, String reason) {
        return CancelTaskCommand.builder()
                .taskId(taskId)
                .operatorId(userId)
                .reason(reason)
                .build();
    }
}
