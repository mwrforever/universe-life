package com.universe.life.task.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.common.result.PageResult;
import com.universe.life.task.privacy.domain.dao.query.TaskAppealQuery;
import com.universe.life.task.privacy.domain.dto.TaskAppealDTO;
import com.universe.life.task.privacy.domain.dto.request.TaskAppealHandleRequest;
import com.universe.life.task.privacy.domain.po.TaskAcceptance;
import com.universe.life.task.privacy.domain.po.TaskAppeal;
import com.universe.life.task.privacy.domain.dto.TaskAcceptanceDTO;
import com.universe.life.task.privacy.domain.vo.TaskAppealStatsVO;
import com.universe.life.task.privacy.domain.vo.TaskAppealVO;
import com.universe.life.task.privacy.enums.TaskAcceptanceStatus;
import com.universe.life.task.privacy.enums.TaskAppealResult;
import com.universe.life.task.privacy.enums.TaskAppealStatus;
import com.universe.life.task.privacy.enums.TaskAppealType;
import com.universe.life.task.privacy.mapper.TaskAcceptanceMapper;
import com.universe.life.task.privacy.mapper.TaskAppealMapper;
import com.universe.life.task.privacy.mapstruct.TaskAcceptanceMapstruct;
import com.universe.life.task.privacy.mapstruct.TaskAppealMapstruct;
import com.universe.life.task.privacy.service.IAdminTaskAppealService;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端申诉服务实现
 * <p>
 * 提供申诉列表查询、申诉详情查看、申诉处理及统计等功能
 * </p>
 *
 * @author universe-life
 */
@Service
@RequiredArgsConstructor
public class AdminTaskAppealServiceImpl implements IAdminTaskAppealService {

    private final TaskAppealMapper appealMapper;
    private final TaskAcceptanceMapper acceptanceMapper;
    private final TaskAppealMapstruct appealMapstruct;
    private final TaskAcceptanceMapstruct acceptanceMapstruct;

    /**
     * 分页查询申诉列表
     *
     * @param query 查询条件，包含分页参数和筛选条件
     * @return 申诉分页结果
     */
    @Override
    public PageResult<TaskAppealVO> pageAppeals(TaskAppealQuery query) {
        // 1. 构建分页对象
        Page<TaskAppealDTO> page = new Page<>(query.getPage(), query.getSize());
        
        // 2. 执行分页查询
        List<TaskAppealDTO> records = appealMapper.selectAppealPage(page, query);
        
        // 3. 空结果处理
        if (CollUtil.isEmpty(records)) {
            return PageResult.empty(page);
        }
        
        // 4. DTO转换为VO并返回
        List<TaskAppealVO> result = appealMapstruct.toVOList(records);
        return PageResult.of(result, page);
    }

    /**
     * 获取申诉详情
     *
     * @param id 申诉ID
     * @return 申诉详情，包含关联的接受记录信息
     * @throws BusinessException.DataNotFoundException 申诉不存在时抛出异常
     */
    @Override
    public TaskAppealVO getAppealDetail(Long id) {
        // 1. 查询申诉基本信息
        TaskAppealDTO appealDTO = appealMapper.selectAppealDetail(id);
        if (appealDTO == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("申诉"));
        }
        
        // 2. DTO转换为VO
        TaskAppealVO vo = appealMapstruct.toVO(appealDTO);
        
        // 3. 查询并设置关联的接受记录信息
        TaskAcceptanceDTO acceptanceDTO = acceptanceMapper.selectAcceptanceDetail(vo.getAcceptanceId());
        if (acceptanceDTO != null) {
            vo.setAcceptanceInfo(acceptanceMapstruct.toVO(acceptanceDTO));
        }
        
        return vo;
    }

    /**
     * 处理申诉
     * <p>
     * 根据处理结果更新申诉状态和关联的接受记录状态
     * </p>
     *
     * @param id      申诉ID
     * @param request 处理请求，包含处理结果和备注
     * @param adminId 管理员ID
     * @throws BusinessException.DataNotFoundException 申诉不存在时抛出异常
     * @throws BusinessException.OperationNotAllowedException 申诉已处理或处理结果不合法时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAppeal(Long id, TaskAppealHandleRequest request, Long adminId) {
        // 1. 查询并校验申诉状态
        TaskAppeal appeal = appealMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.recordNotFound("申诉"));
        }
        if (appeal.getStatus() != TaskAppealStatus.PENDING) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.STATUS_INVALID);
        }

        // 2. 解析并校验处理结果
        TaskAppealResult result = TaskAppealResult.ofCode(request.getResult());
        if (result == null) {
            throw new BusinessException.ParamException(ExceptionMessage.Formatter.paramInvalid("处理结果"));
        }

        // 3. 更新申诉记录
        appeal.setStatus(TaskAppealStatus.HANDLED);
        appeal.setResult(result);
        appeal.setHandlerId(adminId);
        appeal.setHandleRemark(request.getRemark());
        appeal.setHandledAt(LocalDateTime.now());
        appealMapper.updateById(appeal);

        // 4. 根据处理结果更新接受记录状态
        TaskAcceptance acceptance = acceptanceMapper.selectById(appeal.getAcceptanceId());
        TaskAcceptanceStatus newStatus = determineAcceptanceStatus(result, appeal.getAppealType());
        acceptance.setStatus(newStatus);
        acceptanceMapper.updateById(acceptance);
    }

    /**
     * 根据申诉处理结果确定接受记录的新状态
     *
     * @param result     处理结果
     * @param appealType 申诉类型
     * @return 接受记录的新状态
     */
    private TaskAcceptanceStatus determineAcceptanceStatus(TaskAppealResult result, TaskAppealType appealType) {
        if (result == TaskAppealResult.SUPPORT) {
            // 支持申诉方，任务完成
            return TaskAcceptanceStatus.COMPLETED;
        }
        // 不支持申诉方，根据申诉类型决定状态
        if (appealType == TaskAppealType.ACCEPTOR) {
            // 接受者申诉失败，回到待确认状态
            return TaskAcceptanceStatus.WAIT_CONFIRM;
        }
        // 发布者申诉失败，任务完成
        return TaskAcceptanceStatus.COMPLETED;
    }

    /**
     * 获取申诉统计信息
     *
     * @return 申诉统计数据，包含总数、待处理数、已处理数等
     */
    @Override
    public TaskAppealStatsVO getAppealStats() {
        return appealMapper.selectAppealStats();
    }

}
