package com.universe.life.task.privacy.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskAcceptancePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务接受记录PO Mapper
 */
@Mapper
public interface TaskAcceptancePOMapper extends BaseMapper<TaskAcceptancePO> {
}
