package com.universe.life.task.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.universe.life.task.privacy.domain.dao.query.TaskAppealQuery;
import com.universe.life.task.privacy.domain.dto.TaskAppealDTO;
import com.universe.life.task.privacy.domain.po.TaskAppeal;
import com.universe.life.task.privacy.domain.vo.TaskAppealStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务申诉Mapper
 */
@Mapper
public interface TaskAppealMapper extends BaseMapper<TaskAppeal> {

    List<TaskAppealDTO> selectAppealPage(@Param("page")IPage<TaskAppealDTO> page, @Param("query") TaskAppealQuery query);

    long countAppeal(@Param("query") TaskAppealQuery query);

    TaskAppealDTO selectAppealDetail(@Param("id") Long id);

    TaskAppealStatsVO selectAppealStats();

    int checkPendingAppeal(@Param("acceptanceId") Long acceptanceId);
}
