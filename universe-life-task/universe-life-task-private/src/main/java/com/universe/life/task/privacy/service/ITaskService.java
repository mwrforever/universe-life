package com.universe.life.task.privacy.service;

import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskQuery;
import com.universe.life.task.privacy.domain.dto.request.TaskCreateRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskDepositRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskUpdateRequest;
import com.universe.life.task.privacy.domain.vo.TaskCategoryVO;
import com.universe.life.task.privacy.domain.vo.TaskDepositVO;
import com.universe.life.task.privacy.domain.vo.TaskDetailVO;
import com.universe.life.task.privacy.domain.vo.TaskVO;

import java.util.List;

/**
 * 任务服务接口
 *
 * @author universe-life
 */
public interface ITaskService {

    /**
     * 创建任务
     *
     * @param request 创建任务请求
     * @param userId  发布者ID
     * @return 任务ID
     */
    Long createTask(TaskCreateRequest request, Long userId);

    /**
     * 更新任务
     *
     * @param id      任务ID
     * @param request 更新任务请求
     * @param userId  操作用户ID
     */
    void updateTask(Long id, TaskUpdateRequest request, Long userId);

    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDetailVO getTaskDetail(Long id);

    /**
     * 取消任务
     *
     * @param id     任务ID
     * @param userId 操作用户ID
     */
    void cancelTask(Long id, Long userId);

    /**
     * 支付保证金
     *
     * @param id      任务ID
     * @param request 支付保证金请求
     * @param userId  操作用户ID
     * @return 支付信息
     */
    TaskDepositVO payDeposit(Long id, TaskDepositRequest request, Long userId);

    /**
     * 分页查询我发布的任务
     *
     * @param query  查询条件
     * @param userId 用户ID
     * @return 任务分页列表
     */
    PageResult<TaskVO> pagePublishedTasks(TaskQuery query, Long userId);

    /**
     * 分页查询任务大厅
     *
     * @param query 查询条件
     * @return 任务分页列表
     */
    PageResult<TaskVO> pageHallTasks(TaskQuery query);

    /**
     * 获取任务分类列表
     *
     * @return 分类列表
     */
    List<TaskCategoryVO> listCategories();
}
