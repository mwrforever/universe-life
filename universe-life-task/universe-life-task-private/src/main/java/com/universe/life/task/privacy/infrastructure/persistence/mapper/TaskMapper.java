package com.universe.life.task.privacy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Task Mapper
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Mapper
public interface TaskMapper extends BaseMapper<TaskPO> {
}
