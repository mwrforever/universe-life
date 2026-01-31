package com.universe.life.task.privacy.domain.repository;

import com.universe.life.task.privacy.infrastructure.persistence.po.TaskAcceptancePO;

import java.util.List;
import java.util.Optional;

/**
 * 任务接受记录仓储接口
 */
public interface TaskAcceptanceRepository {

    /**
     * 保存接受记录
     *
     * @param acceptance 接受记录
     * @return 保存后的记录
     */
    TaskAcceptancePO save(TaskAcceptancePO acceptance);

    /**
     * 根据ID查询接受记录
     *
     * @param id 记录ID
     * @return 接受记录
     */
    Optional<TaskAcceptancePO> findById(Long id);

    /**
     * 根据任务ID和接单者ID查询接受记录
     *
     * @param taskId     任务ID
     * @param acceptorId 接单者ID
     * @return 接受记录
     */
    Optional<TaskAcceptancePO> findByTaskIdAndAcceptorId(Long taskId, Long acceptorId);

    /**
     * 根据任务ID查询接受记录列表
     *
     * @param taskId 任务ID
     * @return 接受记录列表
     */
    List<TaskAcceptancePO> findByTaskId(Long taskId);

    /**
     * 根据接单者ID查询接受记录列表
     *
     * @param acceptorId 接单者ID
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 接受记录列表
     */
    List<TaskAcceptancePO> findByAcceptorId(Long acceptorId, int pageNum, int pageSize);

    /**
     * 统计接单者的接受记录数量
     *
     * @param acceptorId 接单者ID
     * @return 记录数量
     */
    long countByAcceptorId(Long acceptorId);

    /**
     * 统计任务的待审批申请数量
     *
     * @param taskId 任务ID
     * @return 待审批数量
     */
    long countPendingByTaskId(Long taskId);

    /**
     * 检查用户是否已申请该任务
     *
     * @param taskId     任务ID
     * @param acceptorId 接单者ID
     * @return 是否已申请
     */
    boolean existsByTaskIdAndAcceptorId(Long taskId, Long acceptorId);

    /**
     * 更新接受记录
     *
     * @param acceptance 接受记录
     */
    void update(TaskAcceptancePO acceptance);
}
