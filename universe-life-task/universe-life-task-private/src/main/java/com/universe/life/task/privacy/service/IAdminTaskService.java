package com.universe.life.task.privacy.service;

import com.universe.life.common.result.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskQuery;
import com.universe.life.task.privacy.domain.dto.request.*;
import com.universe.life.task.privacy.domain.vo.TaskCategoryVO;
import com.universe.life.task.privacy.domain.vo.TaskDetailVO;
import com.universe.life.task.privacy.domain.vo.TaskReviewVO;
import com.universe.life.task.privacy.domain.vo.TaskVO;

import java.util.List;

/**
 * 管理端任务服务接口
 *
 * @author universe-life
 */
public interface IAdminTaskService {

    /**
     * 分页查询待审核任务
     *
     * @param query 查询条件
     * @return 任务分页列表
     */
    PageResult<TaskVO> pagePendingTasks(TaskQuery query);

    /**
     * 分页查询全部任务
     *
     * @param query 查询条件
     * @return 任务分页列表
     */
    PageResult<TaskVO> pageAllTasks(TaskQuery query);

    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDetailVO getTaskDetail(Long id);

    /**
     * 获取任务审核记录
     *
     * @param id 任务ID
     * @return 审核记录列表
     */
    List<TaskReviewVO> getTaskReviews(Long id);

    /**
     * 审核通过任务
     *
     * @param id      任务ID
     * @param request 审核通过请求
     * @param adminId 管理员ID
     */
    void approveTask(Long id, TaskApproveRequest request, Long adminId);

    /**
     * 审核拒绝任务
     *
     * @param id      任务ID
     * @param request 审核拒绝请求
     * @param adminId 管理员ID
     */
    void rejectTask(Long id, TaskRejectRequest request, Long adminId);

    /**
     * 强制下架任务
     *
     * @param id      任务ID
     * @param request 下架请求
     * @param adminId 管理员ID
     */
    void offlineTask(Long id, TaskOfflineRequest request, Long adminId);

    /**
     * 获取任务分类列表
     *
     * @return 分类列表
     */
    List<TaskCategoryVO> listCategories();

    /**
     * 创建任务分类
     *
     * @param request 创建分类请求
     */
    void createCategory(TaskCategoryRequest request);

    /**
     * 更新任务分类
     *
     * @param id      分类ID
     * @param request 更新分类请求
     */
    void updateCategory(Long id, TaskCategoryRequest request);

    /**
     * 删除任务分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);
}
