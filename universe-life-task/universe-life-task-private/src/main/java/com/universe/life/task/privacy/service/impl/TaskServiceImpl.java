package com.universe.life.task.privacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskQuery;
import com.universe.life.task.privacy.domain.dto.request.TaskCreateRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskDepositRequest;
import com.universe.life.task.privacy.domain.dto.request.TaskUpdateRequest;
import com.universe.life.task.privacy.domain.po.Task;
import com.universe.life.task.privacy.domain.po.TaskCategory;
import com.universe.life.task.privacy.domain.vo.*;
import com.universe.life.task.privacy.enums.CommonStatus;
import com.universe.life.task.privacy.enums.TaskDepositStatus;
import com.universe.life.task.privacy.enums.TaskReviewStatus;
import com.universe.life.task.privacy.enums.TaskStatus;
import com.universe.life.task.privacy.mapper.TaskCategoryMapper;
import com.universe.life.task.privacy.mapper.TaskMapper;
import com.universe.life.task.privacy.mapstruct.TaskCategoryMapstruct;
import com.universe.life.task.privacy.mapstruct.TaskMapstruct;
import com.universe.life.task.privacy.service.ITaskService;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 任务服务实现
 * <p>
 * 提供任务发布、编辑、取消、支付保证金、任务列表查询等功能
 * </p>
 *
 * @author universe-life
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskMapper taskMapper;
    private final TaskCategoryMapper categoryMapper;
    private final TaskMapstruct taskMapstruct;
    private final TaskCategoryMapstruct categoryMapstruct;

    /**
     * 创建任务
     * <p>
     * 创建新任务，初始状态为待审核，保证金为悬赏金额的50%
     * </p>
     *
     * @param request 创建任务请求
     * @param userId  发布者ID
     * @return 任务ID
     * @throws BusinessException.DataNotFoundException 任务分类不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(TaskCreateRequest request, Long userId) {
        // 1. 校验任务分类是否存在
        TaskCategory category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务分类"));
        }

        // 2. 构建任务实体并设置初始值
        Task task = taskMapstruct.toEntity(request);
        task.setPublisherId(userId);
        task.setDepositAmount(request.getRewardAmount() / 2);
        task.setDepositStatus(TaskDepositStatus.UNPAID);
        task.setCurrentAcceptors(0);
        task.setStatus(TaskStatus.PENDING);
        task.setReviewStatus(TaskReviewStatus.PENDING);

        // 3. 保存任务
        taskMapper.insert(task);
        return task.getId();
    }

    /**
     * 更新任务
     * <p>
     * 仅允许在待审核或已拒绝状态下编辑任务，编辑后重新进入待审核状态
     * </p>
     *
     * @param id      任务ID
     * @param request 更新任务请求
     * @param userId  操作用户ID
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权操作或状态不允许时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(Long id, TaskUpdateRequest request, Long userId) {
        // 1. 查询并校验任务
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }
        if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.REJECTED) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 更新任务字段（仅更新非空字段）
        if (StringUtils.hasText(request.getTitle())) {
            task.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getDescription())) {
            task.setDescription(request.getDescription());
        }
        if (request.getRewardAmount() != null) {
            task.setRewardAmount(request.getRewardAmount());
            task.setDepositAmount(request.getRewardAmount() / 2);
        }
        if (request.getCategoryId() != null) {
            TaskCategory category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务分类"));
            }
            task.setCategoryId(request.getCategoryId());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        if (request.getMaxAcceptors() != null) {
            task.setMaxAcceptors(request.getMaxAcceptors());
        }

        // 3. 如果是已拒绝状态，重新进入待审核
        if (task.getStatus() == TaskStatus.REJECTED) {
            task.setStatus(TaskStatus.PENDING);
            task.setReviewStatus(TaskReviewStatus.PENDING);
        }

        taskMapper.updateById(task);
    }

    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     */
    @Override
    public TaskDetailVO getTaskDetail(Long id) {
        TaskDetailVO vo = taskMapper.selectTaskDetail(id);
        if (vo == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        return vo;
    }

    /**
     * 取消任务
     * <p>
     * 仅允许在待审核或待支付状态下取消任务
     * </p>
     *
     * @param id     任务ID
     * @param userId 操作用户ID
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权操作或状态不允许时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long id, Long userId) {
        // 1. 查询并校验任务
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }
        if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.WAIT_PAY) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 更新任务状态为已取消
        task.setStatus(TaskStatus.CANCELLED);
        taskMapper.updateById(task);
    }

    /**
     * 支付保证金
     * <p>
     * 生成支付订单并返回支付链接，仅允许在待支付状态下操作
     * </p>
     *
     * @param id      任务ID
     * @param request 支付保证金请求
     * @param userId  操作用户ID
     * @return 支付信息，包含订单ID和支付链接
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权操作或状态不允许时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskDepositVO payDeposit(Long id, TaskDepositRequest request, Long userId) {
        // 1. 查询并校验任务
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }
        if (task.getStatus() != TaskStatus.WAIT_PAY) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 生成支付订单（此处为模拟实现，实际应调用支付服务）
        TaskDepositVO vo = new TaskDepositVO();
        vo.setOrderId("PAY" + System.currentTimeMillis());
        vo.setPaymentUrl("https://pay.example.com/" + vo.getOrderId());

        return vo;
    }

    /**
     * 分页查询我发布的任务
     *
     * @param query  查询条件
     * @param userId 用户ID
     * @return 任务分页结果
     */
    @Override
    public PageResult<TaskVO> pagePublishedTasks(TaskQuery query, Long userId) {
        // 1. 设置发布者ID筛选条件
        query.setPublisherId(userId);
        
        // 2. 计算分页偏移量并查询
        int offset = (query.getPage() - 1) * query.getSize();
        List<TaskVO> records = taskMapper.selectTaskPage(query, offset, query.getSize());
        long total = taskMapper.countTask(query);
        
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    /**
     * 分页查询任务大厅
     * <p>
     * 查询所有可接受的任务（进行中状态）
     * </p>
     *
     * @param query 查询条件
     * @return 任务分页结果
     */
    @Override
    public PageResult<TaskVO> pageHallTasks(TaskQuery query) {
        // 1. 计算分页偏移量
        int offset = (query.getPage() - 1) * query.getSize();
        
        // 2. 查询任务大厅列表和总数
        List<TaskVO> records = taskMapper.selectHallTaskPage(query, offset, query.getSize());
        long total = taskMapper.countHallTask(query);
        
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    /**
     * 获取任务分类列表
     * <p>
     * 仅返回启用状态的分类，按排序值升序
     * </p>
     *
     * @return 分类列表
     */
    @Override
    public List<TaskCategoryVO> listCategories() {
        List<TaskCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<TaskCategory>()
                        .eq(TaskCategory::getStatus, CommonStatus.ENABLE)
                        .orderByAsc(TaskCategory::getSort)
        );
        return categoryMapstruct.toVOList(categories);
    }

}
