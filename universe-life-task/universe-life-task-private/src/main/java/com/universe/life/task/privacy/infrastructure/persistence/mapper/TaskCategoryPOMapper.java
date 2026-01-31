package com.universe.life.task.privacy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskCategoryPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务分类PO Mapper
 */
@Mapper
public interface TaskCategoryPOMapper extends BaseMapper<TaskCategoryPO> {
}
