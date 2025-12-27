package com.universe.life.task.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.domain.po.TaskCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务分类Mapper
 */
@Mapper
public interface TaskCategoryMapper extends BaseMapper<TaskCategory> {
}
