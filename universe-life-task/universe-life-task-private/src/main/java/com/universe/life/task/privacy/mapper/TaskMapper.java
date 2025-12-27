package com.universe.life.task.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.domain.dao.query.TaskQuery;
import com.universe.life.task.privacy.domain.po.Task;
import com.universe.life.task.privacy.domain.vo.TaskDetailVO;
import com.universe.life.task.privacy.domain.vo.TaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    List<TaskVO> selectTaskPage(@Param("query") TaskQuery query, @Param("offset") int offset, @Param("limit") int limit);

    long countTask(@Param("query") TaskQuery query);

    List<TaskVO> selectHallTaskPage(@Param("query") TaskQuery query, @Param("offset") int offset, @Param("limit") int limit);

    long countHallTask(@Param("query") TaskQuery query);

    TaskDetailVO selectTaskDetail(@Param("id") Long id);

    List<TaskVO> selectPendingTaskPage(@Param("query") TaskQuery query, @Param("offset") int offset, @Param("limit") int limit);

    long countPendingTask(@Param("query") TaskQuery query);
}
