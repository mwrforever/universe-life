package com.universe.life.task.privacy.service.impl;

import com.universe.life.common.domain.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskAcceptanceQuery;
import com.universe.life.task.privacy.domain.dto.TaskAcceptanceDTO;
import com.universe.life.task.privacy.domain.dto.request.*;
import com.universe.life.task.privacy.domain.po.Task;
import com.universe.life.task.privacy.domain.po.TaskAcceptance;
import com.universe.life.task.privacy.domain.po.TaskAppeal;
import com.universe.life.task.privacy.domain.vo.TaskAcceptanceVO;
import com.universe.life.task.privacy.enums.TaskAcceptanceStatus;
import com.universe.life.task.privacy.enums.TaskAppealStatus;
import com.universe.life.task.privacy.enums.TaskAppealType;
import com.universe.life.task.privacy.enums.TaskStatus;
import com.universe.life.task.privacy.mapper.TaskAcceptanceMapper;
import com.universe.life.task.privacy.mapper.TaskAppealMapper;
import com.universe.life.task.privacy.mapper.TaskMapper;
import com.universe.life.task.privacy.mapstruct.JsonConvertMapstruct;
import com.universe.life.task.privacy.mapstruct.TaskAcceptanceMapstruct;
import com.universe.life.task.privacy.service.ITaskAcceptanceService;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务接受记录服务实现
 * <p>
 * 提供任务接受、提交成果、确认完成、放弃任务、发起申诉等功能
 * </p>
 *
 * @author universe-life
 */
@Service
@RequiredArgsConstructor
public class TaskAcceptanceServiceImpl implements ITaskAcceptanceService {

    private final TaskAcceptanceMapper acceptanceMapper;
    private final TaskMapper taskMapper;
    private final TaskAppealMapper appealMapper;
    private final TaskAcceptanceMapstruct acceptanceMapstruct;

    /**
     * 接受任务
     * <p>
     * 创建任务接受记录，并更新任务的当前接受人数
     * </p>
     *
     * @param taskId 任务ID
     * @param userId 接受者ID
     * @return 接受记录ID
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 状态不允许、接受自己任务或人数已满时抛出异常
     * @throws BusinessException.DataAlreadyExistsException 已接受过时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long acceptTask(Long taskId, Long userId) {
        // 1. 查询并校验任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (task.getStatus() != TaskStatus.PROGRESS) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }
        if (task.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException("不能接受自己发布的任务");
        }
        if (task.getCurrentAcceptors() >= task.getMaxAcceptors()) {
            throw new BusinessException.OperationNotAllowedException("任务接受人数已满");
        }

        // 2. 校验用户是否已接受过此任务
        int count = acceptanceMapper.checkUserAccepted(taskId, userId);
        if (count > 0) {
            throw new BusinessException.DataAlreadyExistsException("已接受过此任务");
        }

        // 3. 创建接受记录，状态为待同意（需发布者确认）
        TaskAcceptance acceptance = new TaskAcceptance();
        acceptance.setTaskId(taskId);
        acceptance.setAcceptorId(userId);
        acceptance.setStatus(TaskAcceptanceStatus.PENDING_APPROVAL);
        acceptance.setAcceptedAt(LocalDateTime.now());
        acceptanceMapper.insert(acceptance);

        // 注意：当前接受人数在发布者同意后才增加

        return acceptance.getId();
    }

    /**
     * 分页查询我的接受记录
     *
     * @param query  查询条件
     * @param userId 用户ID
     * @return 接受记录分页结果
     */
    @Override
    public PageResult<TaskAcceptanceVO> pageMyAcceptances(TaskAcceptanceQuery query, Long userId) {
        // 1. 计算分页偏移量
        int offset = (query.getPage() - 1) * query.getSize();
        
        // 2. 查询接受记录列表
        List<TaskAcceptanceDTO> dtoList = acceptanceMapper.selectMyAcceptancePage(userId, query, offset, query.getSize());
        
        // 3. DTO转换为VO
        List<TaskAcceptanceVO> records = acceptanceMapstruct.toVOList(dtoList);
        
        // 4. 查询总数并返回
        long total = acceptanceMapper.countMyAcceptance(userId, query);
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    /**
     * 分页查询任务的接受记录（发布者查看）
     *
     * @param taskId 任务ID
     * @param query  查询条件
     * @param userId 发布者ID
     * @return 接受记录分页结果
     * @throws BusinessException.DataNotFoundException 任务不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权查看时抛出异常
     */
    @Override
    public PageResult<TaskAcceptanceVO> pageTaskAcceptances(Long taskId, TaskAcceptanceQuery query, Long userId) {
        // 1. 校验任务和权限
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 2. 计算分页偏移量并查询
        int offset = (query.getPage() - 1) * query.getSize();
        List<TaskAcceptanceDTO> dtoList = acceptanceMapper.selectTaskAcceptancePage(taskId, query, offset, query.getSize());
        
        // 3. DTO转换为VO
        List<TaskAcceptanceVO> records = acceptanceMapstruct.toVOList(dtoList);
        
        // 4. 查询总数并返回
        long total = acceptanceMapper.countTaskAcceptance(taskId, query);
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    /**
     * 获取接受记录详情
     *
     * @param id     接受记录ID
     * @param userId 用户ID
     * @return 接受记录详情
     * @throws BusinessException.DataNotFoundException 记录不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权查看时抛出异常
     */
    @Override
    public TaskAcceptanceVO getAcceptanceDetail(Long id, Long userId) {
        // 1. 查询接受记录
        TaskAcceptanceDTO dto = acceptanceMapper.selectAcceptanceDetail(id);
        if (dto == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("接受记录"));
        }
        
        // 2. 校验查看权限（仅接受者或发布者可查看）
        if (!dto.getAcceptorId().equals(userId) && !dto.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }
        
        // 3. DTO转换为VO
        return acceptanceMapstruct.toVO(dto);
    }

    /**
     * 提交任务成果
     * <p>
     * 接受者提交任务完成成果，状态变更为待确认
     * </p>
     *
     * @param id      接受记录ID
     * @param request 提交请求，包含提交内容和图片
     * @param userId  接受者ID
     * @throws BusinessException.DataNotFoundException 记录不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权操作或状态不允许时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTask(Long id, TaskSubmitRequest request, Long userId) {
        // 1. 查询并校验接受记录
        TaskAcceptance acceptance = acceptanceMapper.selectById(id);
        if (acceptance == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("接受记录"));
        }
        if (!acceptance.getAcceptorId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }
        if (acceptance.getStatus() != TaskAcceptanceStatus.PROGRESS) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 设置提交内容和图片
        acceptance.setSubmitContent(request.getContent());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            acceptance.setSubmitImages(JsonConvertMapstruct.stringListToJson(request.getImages()));
        }
        
        // 3. 更新状态为待确认
        acceptance.setStatus(TaskAcceptanceStatus.WAIT_CONFIRM);
        acceptance.setSubmittedAt(LocalDateTime.now());
        acceptanceMapper.updateById(acceptance);
    }

    /**
     * 确认任务完成
     * <p>
     * 发布者确认接受者提交的任务成果
     * </p>
     *
     * @param id      接受记录ID
     * @param request 确认请求，包含是否确认完成
     * @param userId  发布者ID
     * @throws BusinessException.DataNotFoundException 记录不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权操作或状态不允许时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmTask(Long id, TaskConfirmRequest request, Long userId) {
        // 1. 查询接受记录
        TaskAcceptance acceptance = acceptanceMapper.selectById(id);
        if (acceptance == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("接受记录"));
        }

        // 2. 校验操作权限（仅发布者可确认）
        Task task = taskMapper.selectById(acceptance.getTaskId());
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }
        if (acceptance.getStatus() != TaskAcceptanceStatus.WAIT_CONFIRM) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 3. 确认通过则更新状态为已完成
        if (request.getConfirmed()) {
            acceptance.setStatus(TaskAcceptanceStatus.COMPLETED);
            acceptance.setCompletedAt(LocalDateTime.now());
        }
        acceptanceMapper.updateById(acceptance);
    }

    /**
     * 放弃任务
     * <p>
     * 接受者放弃任务，并更新任务的当前接受人数
     * </p>
     *
     * @param id      接受记录ID
     * @param request 放弃请求，包含放弃原因
     * @param userId  接受者ID
     * @throws BusinessException.DataNotFoundException 记录不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权操作或状态不允许时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void abandonTask(Long id, TaskAbandonRequest request, Long userId) {
        // 1. 查询并校验接受记录
        TaskAcceptance acceptance = acceptanceMapper.selectById(id);
        if (acceptance == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("接受记录"));
        }
        if (!acceptance.getAcceptorId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }
        if (acceptance.getStatus() != TaskAcceptanceStatus.PROGRESS) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 更新接受记录状态为已放弃
        acceptance.setStatus(TaskAcceptanceStatus.ABANDONED);
        acceptanceMapper.updateById(acceptance);

        // 3. 更新任务当前接受人数
        Task task = taskMapper.selectById(acceptance.getTaskId());
        task.setCurrentAcceptors(task.getCurrentAcceptors() - 1);
        taskMapper.updateById(task);
    }

    /**
     * 发起申诉
     * <p>
     * 接受者或发布者可对接受记录发起申诉，接受记录状态变更为争议中
     * </p>
     *
     * @param id      接受记录ID
     * @param request 申诉请求，包含申诉原因和证据图片
     * @param userId  申诉人ID
     * @return 申诉ID
     * @throws BusinessException.DataNotFoundException 记录不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权申诉或状态不允许时抛出异常
     * @throws BusinessException.DataAlreadyExistsException 已存在待处理申诉时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long initiateAppeal(Long id, TaskAppealRequest request, Long userId) {
        // 1. 查询接受记录
        TaskAcceptance acceptance = acceptanceMapper.selectById(id);
        if (acceptance == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("接受记录"));
        }

        // 2. 校验申诉权限（仅接受者或发布者可申诉）
        Task task = taskMapper.selectById(acceptance.getTaskId());
        boolean isAcceptor = acceptance.getAcceptorId().equals(userId);
        boolean isPublisher = task.getPublisherId().equals(userId);
        if (!isAcceptor && !isPublisher) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验接受记录状态
        if (acceptance.getStatus() != TaskAcceptanceStatus.WAIT_CONFIRM &&
                acceptance.getStatus() != TaskAcceptanceStatus.COMPLETED) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 校验是否已存在待处理的申诉
        int existingAppeal = appealMapper.checkPendingAppeal(id);
        if (existingAppeal > 0) {
            throw new BusinessException.DataAlreadyExistsException("已存在待处理的申诉");
        }

        // 5. 创建申诉记录
        TaskAppeal appeal = new TaskAppeal();
        appeal.setAcceptanceId(id);
        appeal.setAppellantId(userId);
        appeal.setAppealType(isAcceptor ? TaskAppealType.ACCEPTOR : TaskAppealType.PUBLISHER);
        appeal.setReason(request.getReason());
        if (request.getEvidenceImages() != null && !request.getEvidenceImages().isEmpty()) {
            appeal.setEvidenceImages(JsonConvertMapstruct.stringListToJson(request.getEvidenceImages()));
        }
        appeal.setStatus(TaskAppealStatus.PENDING);
        appealMapper.insert(appeal);

        // 6. 更新接受记录状态为争议中
        acceptance.setStatus(TaskAcceptanceStatus.DISPUTE);
        acceptanceMapper.updateById(acceptance);

        return appeal.getId();
    }

    /**
     * 同意接单申请
     * <p>
     * 发布者同意接单者的申请，接受记录状态从待同意变更为进行中，并更新任务当前接受人数
     * </p>
     *
     * @param id     接受记录ID
     * @param userId 发布者ID
     * @throws BusinessException.DataNotFoundException 记录不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权操作或状态不允许时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAcceptance(Long id, Long userId) {
        // 1. 查询接受记录
        TaskAcceptance acceptance = acceptanceMapper.selectById(id);
        if (acceptance == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("接受记录"));
        }

        // 2. 校验操作权限（仅发布者可同意）
        Task task = taskMapper.selectById(acceptance.getTaskId());
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验接受记录状态
        if (acceptance.getStatus() != TaskAcceptanceStatus.PENDING_APPROVAL) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 校验任务接受人数是否已满
        if (task.getCurrentAcceptors() >= task.getMaxAcceptors()) {
            throw new BusinessException.OperationNotAllowedException("任务接受人数已满");
        }

        // 5. 更新接受记录状态为进行中
        acceptance.setStatus(TaskAcceptanceStatus.PROGRESS);
        acceptanceMapper.updateById(acceptance);

        // 6. 更新任务当前接受人数
        task.setCurrentAcceptors(task.getCurrentAcceptors() + 1);
        taskMapper.updateById(task);
    }

    /**
     * 拒绝接单申请
     * <p>
     * 发布者拒绝接单者的申请，接受记录状态从待同意变更为已拒绝
     * </p>
     *
     * @param id      接受记录ID
     * @param request 拒绝请求，包含拒绝原因
     * @param userId  发布者ID
     * @throws BusinessException.DataNotFoundException 记录不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 无权操作或状态不允许时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectAcceptance(Long id, TaskRejectRequest request, Long userId) {
        // 1. 查询接受记录
        TaskAcceptance acceptance = acceptanceMapper.selectById(id);
        if (acceptance == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("接受记录"));
        }

        // 2. 校验操作权限（仅发布者可拒绝）
        Task task = taskMapper.selectById(acceptance.getTaskId());
        if (task == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("任务"));
        }
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.ACCESS_DENIED);
        }

        // 3. 校验接受记录状态
        if (acceptance.getStatus() != TaskAcceptanceStatus.PENDING_APPROVAL) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 4. 更新接受记录状态为已拒绝，并记录拒绝原因
        acceptance.setStatus(TaskAcceptanceStatus.REJECTED);
        acceptance.setRejectReason(request.getReason());
        acceptanceMapper.updateById(acceptance);
    }

}
