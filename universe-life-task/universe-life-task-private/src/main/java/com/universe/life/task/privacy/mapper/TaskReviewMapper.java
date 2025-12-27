package com.universe.life.task.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.task.privacy.domain.po.TaskReview;
import com.universe.life.task.privacy.domain.vo.TaskReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务审核记录Mapper
 */
@Mapper
public interface TaskReviewMapper extends BaseMapper<TaskReview> {

    List<TaskReviewVO> selectTaskReviews(@Param("taskId") Long taskId);
}
