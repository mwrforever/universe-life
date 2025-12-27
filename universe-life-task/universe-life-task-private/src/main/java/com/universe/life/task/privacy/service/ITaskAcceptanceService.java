package com.universe.life.task.privacy.service;

import com.universe.life.common.result.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskAcceptanceQuery;
import com.universe.life.task.privacy.domain.dto.request.*;
import com.universe.life.task.privacy.domain.vo.TaskAcceptanceVO;

/**
 * 任务接受记录服务接口
 *
 * @author universe-life
 */
public interface ITaskAcceptanceService {

    /**
     * 接受任务
     *
     * @param taskId 任务ID
     * @param userId 接受者ID
     * @return 接受记录ID
     */
    Long acceptTask(Long taskId, Long userId);

    /**
     * 分页查询我的接受记录
     *
     * @param query  查询条件
     * @param userId 用户ID
     * @return 接受记录分页列表
     */
    PageResult<TaskAcceptanceVO> pageMyAcceptances(TaskAcceptanceQuery query, Long userId);

    /**
     * 分页查询任务的接受记录（发布者查看）
     *
     * @param taskId 任务ID
     * @param query  查询条件
     * @param userId 发布者ID
     * @return 接受记录分页列表
     */
    PageResult<TaskAcceptanceVO> pageTaskAcceptances(Long taskId, TaskAcceptanceQuery query, Long userId);

    /**
     * 获取接受记录详情
     *
     * @param id     接受记录ID
     * @param userId 用户ID
     * @return 接受记录详情
     */
    TaskAcceptanceVO getAcceptanceDetail(Long id, Long userId);

    /**
     * 提交任务成果
     *
     * @param id      接受记录ID
     * @param request 提交请求
     * @param userId  接受者ID
     */
    void submitTask(Long id, TaskSubmitRequest request, Long userId);

    /**
     * 确认任务完成
     *
     * @param id      接受记录ID
     * @param request 确认请求
     * @param userId  发布者ID
     */
    void confirmTask(Long id, TaskConfirmRequest request, Long userId);

    /**
     * 放弃任务
     *
     * @param id      接受记录ID
     * @param request 放弃请求
     * @param userId  接受者ID
     */
    void abandonTask(Long id, TaskAbandonRequest request, Long userId);

    /**
     * 发起申诉
     *
     * @param id      接受记录ID
     * @param request 申诉请求
     * @param userId  申诉人ID
     * @return 申诉ID
     */
    Long initiateAppeal(Long id, TaskAppealRequest request, Long userId);

    /**
     * 同意接单申请
     * <p>
     * 发布者同意接单者的申请，接受记录状态从待同意变更为进行中
     * </p>
     *
     * @param id     接受记录ID
     * @param userId 发布者ID
     */
    void approveAcceptance(Long id, Long userId);

    /**
     * 拒绝接单申请
     * <p>
     * 发布者拒绝接单者的申请，接受记录状态从待同意变更为已拒绝
     * </p>
     *
     * @param id      接受记录ID
     * @param request 拒绝请求，包含拒绝原因
     * @param userId  发布者ID
     */
    void rejectAcceptance(Long id, TaskRejectRequest request, Long userId);
}
