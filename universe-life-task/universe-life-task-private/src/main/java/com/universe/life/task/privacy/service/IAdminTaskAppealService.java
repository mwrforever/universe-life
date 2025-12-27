package com.universe.life.task.privacy.service;

import com.universe.life.common.result.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskAppealQuery;
import com.universe.life.task.privacy.domain.dto.request.TaskAppealHandleRequest;
import com.universe.life.task.privacy.domain.vo.TaskAppealStatsVO;
import com.universe.life.task.privacy.domain.vo.TaskAppealVO;

/**
 * 管理端申诉服务接口
 *
 * @author universe-life
 */
public interface IAdminTaskAppealService {

    /**
     * 分页查询申诉列表
     *
     * @param query 查询条件
     * @return 申诉分页列表
     */
    PageResult<TaskAppealVO> pageAppeals(TaskAppealQuery query);

    /**
     * 获取申诉详情
     *
     * @param id 申诉ID
     * @return 申诉详情
     */
    TaskAppealVO getAppealDetail(Long id);

    /**
     * 处理申诉
     *
     * @param id      申诉ID
     * @param request 处理请求
     * @param adminId 管理员ID
     */
    void handleAppeal(Long id, TaskAppealHandleRequest request, Long adminId);

    /**
     * 获取申诉统计信息
     *
     * @return 申诉统计
     */
    TaskAppealStatsVO getAppealStats();
}
