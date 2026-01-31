package com.universe.life.task.privacy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务PO Mapper
 */
@Mapper
public interface TaskPOMapper extends BaseMapper<TaskPO> {
}
