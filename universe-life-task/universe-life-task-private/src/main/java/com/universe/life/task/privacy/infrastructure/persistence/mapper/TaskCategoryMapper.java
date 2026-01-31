package com.universe.life.task.privacy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskCategoryPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * TaskCategory Mapper
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Mapper
public interface TaskCategoryMapper extends BaseMapper<TaskCategoryPO> {
}
