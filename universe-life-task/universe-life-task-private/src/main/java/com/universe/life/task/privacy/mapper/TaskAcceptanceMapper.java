package com.universe.life.task.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.domain.dao.query.TaskAcceptanceQuery;
import com.universe.life.task.privacy.domain.dto.TaskAcceptanceDTO;
import com.universe.life.task.privacy.domain.po.TaskAcceptance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务接受记录Mapper
 */
@Mapper
public interface TaskAcceptanceMapper extends BaseMapper<TaskAcceptance> {

    List<TaskAcceptanceDTO> selectMyAcceptancePage(@Param("userId") Long userId, @Param("query") TaskAcceptanceQuery query, 
                                                   @Param("offset") int offset, @Param("limit") int limit);

    long countMyAcceptance(@Param("userId") Long userId, @Param("query") TaskAcceptanceQuery query);

    List<TaskAcceptanceDTO> selectTaskAcceptancePage(@Param("taskId") Long taskId, @Param("query") TaskAcceptanceQuery query,
                                                     @Param("offset") int offset, @Param("limit") int limit);

    long countTaskAcceptance(@Param("taskId") Long taskId, @Param("query") TaskAcceptanceQuery query);

    TaskAcceptanceDTO selectAcceptanceDetail(@Param("id") Long id);
}
