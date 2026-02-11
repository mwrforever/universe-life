package com.universe.life.task.privacy.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.universe.life.task.privacy.application.assembler.TaskCategoryAssembler;
import com.universe.life.task.privacy.domain.model.valueobject.TaskCategory;
import com.universe.life.task.privacy.domain.repository.TaskCategoryRepository;
import com.universe.life.task.privacy.infrastructure.enums.CommonStatus;
import com.universe.life.task.privacy.infrastructure.persistence.mapper.TaskCategoryMapper;
import com.universe.life.task.privacy.infrastructure.persistence.po.TaskCategoryPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TaskCategory 仓储实现
 * 
 * @author Universe Life Team
 * @since 2025-01-18
 */
@Repository
@RequiredArgsConstructor
public class TaskCategoryRepositoryImpl implements TaskCategoryRepository {

    private final TaskCategoryMapper categoryMapper;
    private final TaskCategoryAssembler taskCategoryAssembler;

    @Override
    public TaskCategory save(TaskCategory category) {
        TaskCategoryPO po = taskCategoryAssembler.toTaskCategoryPO(category);
        if (po.getCategoryId() == null) {
            categoryMapper.insert(po);
        } else {
            categoryMapper.updateById(po);
        }
        return taskCategoryAssembler.toTaskCategory(po);
    }

    @Override
    public Optional<TaskCategory> findById(Long categoryId) {
        TaskCategoryPO po = categoryMapper.selectById(categoryId);
        return Optional.ofNullable(po).map(taskCategoryAssembler::toTaskCategory);
    }

    @Override
    public List<TaskCategory> findAll(CommonStatus status) {
        LambdaQueryWrapper<TaskCategoryPO> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(TaskCategoryPO::getStatus, status);
        }
        wrapper.orderByAsc(TaskCategoryPO::getSort);
        
        List<TaskCategoryPO> poList = categoryMapper.selectList(wrapper);
        return poList.stream().map(taskCategoryAssembler::toTaskCategory).collect(Collectors.toList());
    }

    @Override
    public Optional<TaskCategory> findByName(String name) {
        LambdaQueryWrapper<TaskCategoryPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCategoryPO::getName, name);
        TaskCategoryPO po = categoryMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(taskCategoryAssembler::toTaskCategory);
    }

    @Override
    public Optional<TaskCategory> findByCode(String code) {
        LambdaQueryWrapper<TaskCategoryPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCategoryPO::getCode, code);
        TaskCategoryPO po = categoryMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(taskCategoryAssembler::toTaskCategory);
    }

    @Override
    public boolean existsByNameExcludingId(String name, Long excludeId) {
        LambdaQueryWrapper<TaskCategoryPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCategoryPO::getName, name);
        if (excludeId != null) {
            wrapper.ne(TaskCategoryPO::getCategoryId, excludeId);
        }
        return categoryMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByCodeExcludingId(String code, Long excludeId) {
        LambdaQueryWrapper<TaskCategoryPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCategoryPO::getCode, code);
        if (excludeId != null) {
            wrapper.ne(TaskCategoryPO::getCategoryId, excludeId);
        }
        return categoryMapper.selectCount(wrapper) > 0;
    }

}
