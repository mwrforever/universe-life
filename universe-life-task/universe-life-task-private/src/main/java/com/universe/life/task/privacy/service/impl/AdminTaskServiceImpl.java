package com.universe.life.task.privacy.service.impl;

import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskQuery;
import com.universe.life.task.privacy.domain.dto.request.*;
import com.universe.life.task.privacy.domain.po.Task;
import com.universe.life.task.privacy.domain.po.TaskCategory;
import com.universe.life.task.privacy.domain.po.TaskReview;
import com.universe.life.task.privacy.domain.vo.TaskCategoryVO;
import com.universe.life.task.privacy.domain.vo.TaskDetailVO;
import com.universe.life.task.privacy.domain.vo.TaskReviewVO;
import com.universe.life.task.privacy.domain.vo.TaskVO;
import com.universe.life.task.privacy.enums.CommonStatus;
import com.universe.life.task.privacy.enums.TaskReviewStatus;
import com.universe.life.task.privacy.enums.TaskStatus;
import com.universe.life.task.privacy.mapper.TaskCategoryMapper;
import com.universe.life.task.privacy.mapper.TaskMapper;
import com.universe.life.task.privacy.mapper.TaskReviewMapper;
import com.universe.life.task.privacy.mapstruct.TaskCategoryMapstruct;
import com.universe.life.task.privacy.service.IAdminTaskService;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端任务服务实现
 * <p>
 * 提供任务审核、任务列表查询、任务分类管理等功能
 * </p>
 *
 * @author universe-life
 */
@Service
@RequiredArgsConstructor
public class AdminTaskServiceImpl implements IAdminTaskService {

    private final TaskMapper taskMapper;
    private final TaskCategoryMapper categoryMapper;
    private final TaskReviewMapper reviewMapper;
    private final TaskCategoryMapstruct categoryMapstruct;

    /**
     * 分页查询待审核任务列表
     *
     * @param query 查询条件
     * @return 待审核任务分页结果
     */
    @Override
    public PageResult<TaskVO> pagePendingTasks(TaskQuery query) {
        // 1. 计算分页偏移量
        int offset = (query.getPage() - 1) * query.getSize();
        
        // 2. 查询待审核任务列表和总数
        List<TaskVO> records = taskMapper.selectPendingTaskPage(query, offset, query.getSize());
        long total = taskMapper.countPendingTask(query);
        
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    /**
     * 分页查询全部任务列表
     *
     * @param query 查询条件
     * @return 任务分页结果
     */
    @Override
    public PageResult<TaskVO> pageAllTasks(TaskQuery query) {
        // 1. 计算分页偏移量
        int offset = (query.getPage() - 1) * query.getSize();
        
        // 2. 查询任务列表和总数
        List<TaskVO> records = taskMapper.selectTaskPage(query, offset, query.getSize());
        long total = taskMapper.countTask(query);
        
        return PageResult.of(records, total, query.getPage(), query.getSize());
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
     * 获取任务审核记录列表
     *
     * @param id 任务ID
     * @return 审核记录列表
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     */
    @Override
    public List<TaskReviewVO> getTaskReviews(Long id) {
        // 1. 校验任务是否存在
        boolean taskExists = new LambdaQueryChainWrapper<>(taskMapper)
                .eq(Task::getId, id)
                .exists();
        if (!taskExists) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        
        // 2. 查询审核记录
        return reviewMapper.selectTaskReviews(id);
    }

    /**
     * 审核通过任务
     * <p>
     * 更新任务审核状态为已通过，任务状态为待支付，并记录审核日志
     * </p>
     *
     * @param id      任务ID
     * @param request 审核通过请求
     * @param adminId 管理员ID
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 任务已审核时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(Long id, TaskApproveRequest request, Long adminId) {
        // 1. 查询并校验任务状态
        Task task = new LambdaQueryChainWrapper<>(taskMapper)
                .select(Task::getId, Task::getReviewStatus, Task::getStatus, Task::getVersion)
                .eq(Task::getId, id)
                .one();
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (task.getReviewStatus() != TaskReviewStatus.PENDING) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 更新任务状态
        task.setReviewStatus(TaskReviewStatus.APPROVED);
        task.setStatus(TaskStatus.WAIT_PAY);
        taskMapper.updateById(task);

        // 3. 记录审核日志
        TaskReview review = new TaskReview();
        review.setTaskId(id);
        review.setReviewerId(adminId);
        review.setStatus(TaskReviewStatus.APPROVED);
        review.setReviewedAt(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    /**
     * 审核拒绝任务
     * <p>
     * 更新任务审核状态为已拒绝，任务状态为已拒绝，并记录审核日志
     * </p>
     *
     * @param id      任务ID
     * @param request 审核拒绝请求，包含拒绝原因
     * @param adminId 管理员ID
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 任务已审核时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(Long id, TaskRejectRequest request, Long adminId) {
        // 1. 查询并校验任务状态
        Task task = new LambdaQueryChainWrapper<>(taskMapper)
                .select(Task::getId, Task::getReviewStatus, Task::getStatus, Task::getVersion)
                .eq(Task::getId, id)
                .one();
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (task.getReviewStatus() != TaskReviewStatus.PENDING) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 更新任务状态
        task.setReviewStatus(TaskReviewStatus.REJECTED);
        task.setStatus(TaskStatus.REJECTED);
        taskMapper.updateById(task);

        // 3. 记录审核日志
        TaskReview review = new TaskReview();
        review.setTaskId(id);
        review.setReviewerId(adminId);
        review.setStatus(TaskReviewStatus.REJECTED);
        review.setRejectReason(request.getReason());
        review.setReviewedAt(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    /**
     * 强制下架任务
     * <p>
     * 将任务状态更新为已下架，仅允许对进行中或待支付的任务执行
     * </p>
     *
     * @param id      任务ID
     * @param request 下架请求，包含下架原因
     * @param adminId 管理员ID
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 状态不允许下架时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineTask(Long id, TaskOfflineRequest request, Long adminId) {
        // 1. 查询并校验任务状态
        Task task = new LambdaQueryChainWrapper<>(taskMapper)
                .select(Task::getId, Task::getStatus, Task::getVersion)
                .eq(Task::getId, id)
                .one();
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (task.getStatus() != TaskStatus.PROGRESS && task.getStatus() != TaskStatus.WAIT_PAY) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 更新任务状态为已下架
        task.setStatus(TaskStatus.OFFLINE);
        taskMapper.updateById(task);
    }

    /**
     * 获取任务分类列表
     *
     * @return 分类列表，按排序值升序
     */
    @Override
    public List<TaskCategoryVO> listCategories() {
        List<TaskCategory> categories = new LambdaQueryChainWrapper<>(categoryMapper)
                .select(TaskCategory::getId, TaskCategory::getName, TaskCategory::getCode,
                        TaskCategory::getSort, TaskCategory::getStatus)
                .orderByAsc(TaskCategory::getSort)
                .list();
        return categoryMapstruct.toVOList(categories);
    }

    /**
     * 创建任务分类
     *
     * @param request 创建分类请求
     * @throws BusinessException.DataAlreadyExistsException 分类编码已存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(TaskCategoryRequest request) {
        // 1. 校验分类编码唯一性
        boolean codeExists = new LambdaQueryChainWrapper<>(categoryMapper)
                .eq(TaskCategory::getCode, request.getCode())
                .exists();
        if (codeExists) {
            throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.Formatter.recordAlreadyExists("分类编码"));
        }

        // 2. 构建并保存分类实体
        TaskCategory category = categoryMapstruct.toEntity(request);
        category.setStatus(request.getStatus() != null 
                ? CommonStatus.ofCode(request.getStatus()) 
                : CommonStatus.ENABLE);
        categoryMapper.insert(category);
    }

    /**
     * 更新任务分类
     *
     * @param id      分类ID
     * @param request 更新分类请求
     * @throws BusinessException.DataNotFoundException 分类不存在时抛出异常
     * @throws BusinessException.DataAlreadyExistsException 编码已存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, TaskCategoryRequest request) {
        // 1. 校验分类是否存在
        TaskCategory category = new LambdaQueryChainWrapper<>(categoryMapper)
                .select(TaskCategory::getId, TaskCategory::getName, TaskCategory::getCode,
                        TaskCategory::getSort, TaskCategory::getStatus)
                .eq(TaskCategory::getId, id)
                .one();
        if (category == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("分类"));
        }

        // 2. 校验分类编码唯一性（排除当前分类）
        boolean codeExists = new LambdaQueryChainWrapper<>(categoryMapper)
                .eq(TaskCategory::getCode, request.getCode())
                .ne(TaskCategory::getId, id)
                .exists();
        if (codeExists) {
            throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.Formatter.recordAlreadyExists("分类编码"));
        }

        // 3. 更新分类信息
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setSort(request.getSort());
        if (request.getStatus() != null) {
            category.setStatus(CommonStatus.ofCode(request.getStatus()));
        }
        categoryMapper.updateById(category);
    }

    /**
     * 删除任务分类
     * <p>
     * 仅允许删除没有关联任务的分类
     * </p>
     *
     * @param id 分类ID
     * @throws BusinessException.DataNotFoundException 分类不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 分类下存在任务时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 1. 校验分类是否存在
        boolean categoryExists = new LambdaQueryChainWrapper<>(categoryMapper)
                .eq(TaskCategory::getId, id)
                .exists();
        if (!categoryExists) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("分类"));
        }

        // 2. 校验分类下是否存在任务
        boolean hasTask = new LambdaQueryChainWrapper<>(taskMapper)
                .eq(Task::getCategoryId, id)
                .exists();
        if (hasTask) {
            throw new BusinessException.OperationNotAllowedException("分类下存在任务，无法删除");
        }

        // 3. 删除分类
        categoryMapper.deleteById(id);
    }

}

