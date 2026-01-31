package com.universe.life.task.privacy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.universe.life.task.privacy.domain.repository.TaskAcceptanceRepository;
import com.universe.life.task.privacy.infrastructure.persistence.mapper.TaskAcceptancePOMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskAcceptancePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务接受记录仓储实现
 * <p>
 * 负责任务接受记录的持久化操作，包括：
 * <ul>
 *   <li>接受记录的增删改查</li>
 *   <li>按任务ID或接单者ID查询记录</li>
 *   <li>统计待审批申请数量</li>
 * </ul>
 * </p>
 * <p>
 * 注意：TaskAcceptance作为简单实体，直接使用PO进行操作，
 * 不需要额外的领域对象转换。
 * </p>
 *
 * @author universe-life
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class TaskAcceptanceRepositoryImpl implements TaskAcceptanceRepository {

    /** MyBatis Mapper */
    private final TaskAcceptancePOMapper acceptanceMapper;

    /** 待审批状态码 */
    private static final Integer PENDING_STATUS = 0;

    /**
     * 保存接受记录
     * <p>
     * 根据ID判断是新增还是更新：
     * - ID为空：执行插入操作
     * - ID不为空：执行更新操作
     * </p>
     *
     * @param acceptance 接受记录
     * @return 保存后的记录（包含ID）
     */
    @Override
    public TaskAcceptancePO save(TaskAcceptancePO acceptance) {
        if (acceptance.getId() == null) {
            // 新增记录
            acceptanceMapper.insert(acceptance);
        } else {
            // 更新记录
            acceptanceMapper.updateById(acceptance);
        }
        return acceptance;
    }

    /**
     * 根据ID查询接受记录
     *
     * @param id 记录ID
     * @return 接受记录（Optional包装）
     */
    @Override
    public Optional<TaskAcceptancePO> findById(Long id) {
        return Optional.ofNullable(acceptanceMapper.selectById(id));
    }

    /**
     * 根据任务ID和接单者ID查询接受记录
     * <p>
     * 用于检查用户是否已申请某个任务。
     * </p>
     *
     * @param taskId     任务ID
     * @param acceptorId 接单者ID
     * @return 接受记录（Optional包装）
     */
    @Override
    public Optional<TaskAcceptancePO> findByTaskIdAndAcceptorId(Long taskId, Long acceptorId) {
        LambdaQueryWrapper<TaskAcceptancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAcceptancePO::getTaskId, taskId)
                .eq(TaskAcceptancePO::getAcceptorId, acceptorId);
        return Optional.ofNullable(acceptanceMapper.selectOne(wrapper));
    }

    /**
     * 根据任务ID查询接受记录列表
     * <p>
     * 按创建时间降序排列，用于查看某个任务的所有申请记录。
     * </p>
     *
     * @param taskId 任务ID
     * @return 接受记录列表
     */
    @Override
    public List<TaskAcceptancePO> findByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskAcceptancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAcceptancePO::getTaskId, taskId)
                .orderByDesc(TaskAcceptancePO::getCreatedAt);
        return acceptanceMapper.selectList(wrapper);
    }

    /**
     * 根据接单者ID查询接受记录列表（分页）
     * <p>
     * 按创建时间降序排列，用于查看用户的接单历史。
     * </p>
     *
     * @param acceptorId 接单者ID
     * @param pageNum    页码（从1开始）
     * @param pageSize   每页数量
     * @return 接受记录列表
     */
    @Override
    public List<TaskAcceptancePO> findByAcceptorId(Long acceptorId, int pageNum, int pageSize) {
        LambdaQueryWrapper<TaskAcceptancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAcceptancePO::getAcceptorId, acceptorId)
                .orderByDesc(TaskAcceptancePO::getCreatedAt);

        Page<TaskAcceptancePO> page = new Page<>(pageNum, pageSize);
        return acceptanceMapper.selectPage(page, wrapper).getRecords();
    }

    /**
     * 统计接单者的接受记录数量
     *
     * @param acceptorId 接单者ID
     * @return 记录数量
     */
    @Override
    public long countByAcceptorId(Long acceptorId) {
        LambdaQueryWrapper<TaskAcceptancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAcceptancePO::getAcceptorId, acceptorId);
        return acceptanceMapper.selectCount(wrapper);
    }

    /**
     * 统计任务的待审批申请数量
     * <p>
     * 用于判断任务是否有待处理的申请。
     * </p>
     *
     * @param taskId 任务ID
     * @return 待审批数量
     */
    @Override
    public long countPendingByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskAcceptancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAcceptancePO::getTaskId, taskId)
                .eq(TaskAcceptancePO::getStatus, PENDING_STATUS);
        return acceptanceMapper.selectCount(wrapper);
    }

    /**
     * 检查用户是否已申请该任务
     * <p>
     * 用于防止重复申请。
     * </p>
     *
     * @param taskId     任务ID
     * @param acceptorId 接单者ID
     * @return 是否已申请
     */
    @Override
    public boolean existsByTaskIdAndAcceptorId(Long taskId, Long acceptorId) {
        LambdaQueryWrapper<TaskAcceptancePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAcceptancePO::getTaskId, taskId)
                .eq(TaskAcceptancePO::getAcceptorId, acceptorId);
        return acceptanceMapper.selectCount(wrapper) > 0;
    }

    /**
     * 更新接受记录
     *
     * @param acceptance 接受记录
     */
    @Override
    public void update(TaskAcceptancePO acceptance) {
        acceptanceMapper.updateById(acceptance);
    }
}
