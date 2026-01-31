package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.user.privacy.constants.RedisConstants;
import com.universe.life.user.privacy.domain.dto.request.BatchUserDepartmentRequest;
import com.universe.life.user.privacy.domain.dto.request.UserDepartmentRequest;
import com.universe.life.user.privacy.domain.po.SysDepartment;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.po.SysUserDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.domain.vo.SysUserSimpleVO;
import com.universe.life.user.privacy.mapper.SysDepartmentMapper;
import com.universe.life.user.privacy.mapper.SysUserDepartmentMapper;
import com.universe.life.user.privacy.mapper.SysUserMapper;
import com.universe.life.user.privacy.service.ISysUserDepartmentService;
import com.universe.life.common.util.CacheUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户部门关联服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserDepartmentServiceImpl extends ServiceImpl<SysUserDepartmentMapper, SysUserDepartment> implements ISysUserDepartmentService {

    private final SysUserMapper sysUserMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final CacheUtil cacheUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserToDepartment(UserDepartmentRequest request) {
        log.info("添加用户到部门，用户ID：{}，部门ID：{}", request.getUserId(), request.getDepartmentId());

        // 验证用户和部门是否存在
        validateUserAndDepartment(request.getUserId(), request.getDepartmentId());

        // 检查关系是否已存在
        long count = count(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getUserId, request.getUserId())
                .eq(SysUserDepartment::getDepartmentId, request.getDepartmentId()));

        if (count > 0) {
            throw new BusinessException.DataAlreadyExistsException("用户已在该部门中");
        }

        // 创建关联
        SysUserDepartment userDepartment = new SysUserDepartment();
        userDepartment.setUserId(request.getUserId());
        userDepartment.setDepartmentId(request.getDepartmentId());
        userDepartment.setIsPrimary(request.getIsPrimary());

        try {
            save(userDepartment);
        } catch (DuplicateKeyException e) {
            throw new BusinessException.DataAlreadyExistsException("用户已在该部门中");
        }

        // 删除缓存
        invalidateCache(request.getUserId(), request.getDepartmentId());

        log.info("添加用户到部门成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserFromDepartment(Long userId, Long departmentId) {
        log.info("从部门移除用户，用户ID：{}，部门ID：{}", userId, departmentId);

        // 检查关系是否存在
        long count = count(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getUserId, userId)
                .eq(SysUserDepartment::getDepartmentId, departmentId));

        if (count == 0) {
            throw new BusinessException.DataNotFoundException("用户部门关系不存在");
        }

        // 删除关联
        boolean removed = remove(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getUserId, userId)
                .eq(SysUserDepartment::getDepartmentId, departmentId));

        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户部门关系删除"));
        }

        // 删除缓存
        invalidateCache(userId, departmentId);

        log.info("从部门移除用户成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddUsersToDepartment(BatchUserDepartmentRequest request) {
        log.info("批量添加用户到部门，部门ID：{}，用户ID列表：{}", request.getDepartmentId(), request.getUserIds());

        // 验证部门是否存在
        SysDepartment department = sysDepartmentMapper.selectById(request.getDepartmentId());
        if (department == null || department.getDeleted()) {
            throw new BusinessException.DataNotFoundException("部门不存在");
        }
        if (CollUtil.isEmpty(request.getUserIds())) {
            throw new BusinessException.ParamException(ExceptionMessage.USER_INFO_CAN_NOT_BE_EMPTY);
        }

        // 检查用户是否存在（批量查询，避免for循环）
        long existsUserCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, request.getUserIds())
                .eq(SysUser::getDeleted, false));
        if (existsUserCount != request.getUserIds().size()) {
            throw new BusinessException.DataNotFoundException("部分用户不存在");
        }

        // 查询已存在的用户部门关系（批量查询，避免for循环）
        List<SysUserDepartment> existingRelations = lambdaQuery()
                .select(SysUserDepartment::getUserId)
                .eq(SysUserDepartment::getDepartmentId, request.getDepartmentId())
                .in(SysUserDepartment::getUserId, request.getUserIds())
                .list();

        // 过滤掉已存在的关系
        Set<Long> existingUserIds = existingRelations.stream()
                .map(SysUserDepartment::getUserId)
                .collect(Collectors.toSet());
        
        List<Long> newUserIds = request.getUserIds().stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(newUserIds)) {
            log.warn("所有用户已在该部门中，无需添加");
            return;
        }

        // 创建关联列表
        List<SysUserDepartment> userDepartments = newUserIds.stream()
                .map(userId -> new SysUserDepartment()
                        .setUserId(userId)
                        .setDepartmentId(request.getDepartmentId())
                        .setIsPrimary(request.getIsPrimary())
                ).toList();

        // 批量保存
        saveBatch(userDepartments);

        // 删除缓存
        Set<String> keys = getKeys(newUserIds, request.getDepartmentId());
        cacheUtil.deleteAll(keys);
        log.info("批量添加用户到部门成功，成功数量：{}", userDepartments.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveUsersFromDepartment(BatchUserDepartmentRequest request) {
        log.info("批量从部门移除用户，部门ID：{}，用户ID列表：{}", request.getDepartmentId(), request.getUserIds());

        // 删除关联
        boolean removed = remove(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getDepartmentId, request.getDepartmentId())
                .in(SysUserDepartment::getUserId, request.getUserIds()));

        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("批量删除用户部门关系"));
        }

        // 删除缓存
        Set<String> keys = getKeys(request.getUserIds(), request.getDepartmentId());
        cacheUtil.deleteAll(keys);

        log.info("批量从部门移除用户成功");
    }

    private static @NonNull Set<String> getKeys(List<Long> userIds, Long departmentId) {
        Set<String> keys = userIds.stream().map(userId -> RedisConstants.USER_DEPARTMENTS_KEY + userId).collect(Collectors.toSet());
        keys.add(RedisConstants.DEPARTMENT_USERS_KEY + departmentId);
        return keys;
    }

    @Override
    public List<SysUserSimpleVO> getUsersByDepartment(Long departmentId) {
        // 1. 尝试从缓存获取
        String cacheKey = RedisConstants.DEPARTMENT_USERS_KEY + departmentId;
        List<SysUserSimpleVO> cached = cacheUtil.getList(cacheKey, SysUserSimpleVO.class);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中，查询数据库
        List<SysUserSimpleVO> users = baseMapper.selectUsersByDepartmentId(departmentId);

        // 3. 写入缓存
        cacheUtil.set(cacheKey, users, RedisConstants.getDepartmentUsersExpire());

        return users;
    }

    @Override
    public List<SysDepartmentSimpleVO> getDepartmentsByUser(Long userId) {
        // 1. 尝试从缓存获取
        String cacheKey = RedisConstants.USER_DEPARTMENTS_KEY + userId;
        List<SysDepartmentSimpleVO> cached = cacheUtil.getList(cacheKey, SysDepartmentSimpleVO.class);
        if (cached != null) {
            log.debug("缓存命中: {}", cacheKey);
            return cached;
        }

        // 2. 缓存未命中，查询数据库
        log.debug("缓存未命中: {}", cacheKey);
        List<SysDepartmentSimpleVO> departments = baseMapper.selectDepartmentsByUserId(userId);

        // 3. 写入缓存
        cacheUtil.set(cacheKey, departments, RedisConstants.getUserDepartmentsExpire());

        return departments;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserDepartments(Long userId, List<Long> departmentIds, Long primaryDepartmentId) {
        log.info("分配用户部门，用户ID：{}，部门ID列表：{}", userId, departmentIds);

        // 先删除用户原有的部门关联
        removeUserDepartments(userId);

        if (CollUtil.isEmpty(departmentIds)) {
            return;
        }

        // 创建新的部门关联

        List<SysUserDepartment> userDepartments = departmentIds.stream().map(departmentId -> new SysUserDepartment()
                .setDepartmentId(departmentId)
                .setUserId(userId)
                .setIsPrimary(departmentId.equals(primaryDepartmentId))
        ).toList();

        // 如果没有指定主部门，默认第一个为主部门
        if (primaryDepartmentId == null && !userDepartments.isEmpty()) {
            userDepartments.get(0).setIsPrimary(true);
        }

        saveBatch(userDepartments);

        // 删除缓存
        Set<String> keys = getUserDepartmentKeys(departmentIds, userId);
        cacheUtil.deleteAll(keys);

        log.info("分配用户部门成功，用户ID：{}", userId);
    }

    @Override
    public List<SysDepartmentSimpleVO> getUserDepartments(Long userId) {
        return getDepartmentsByUser(userId);
    }

    @Override
    public SysDepartmentSimpleVO getUserPrimaryDepartment(Long userId) {
        return baseMapper.selectPrimaryDepartmentByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserDepartments(Long userId) {
        // 获取用户的所有部门ID
        List<SysUserDepartment> list = lambdaQuery().select(SysUserDepartment::getDepartmentId).eq(SysUserDepartment::getUserId, userId).list();

        boolean removed = lambdaUpdate()
                .eq(SysUserDepartment::getUserId, userId)
                .remove();
        if (!removed) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("批量删除用户部门关系"));
        }
        // 删除缓存
        Set<String> keys = list.stream().map(sds -> RedisConstants.DEPARTMENT_USERS_KEY + sds.getDepartmentId()).collect(Collectors.toSet());
        keys.add(RedisConstants.USER_DEPARTMENTS_KEY + userId);
        cacheUtil.deleteAll(keys);
    }

    private Set<String> getUserDepartmentKeys(List<Long> departmentIds, Long userId) {
        Set<String> keys = departmentIds.stream().map(departmentId -> RedisConstants.DEPARTMENT_USERS_KEY + departmentId).collect(Collectors.toSet());
        keys.add(RedisConstants.USER_DEPARTMENTS_KEY + userId);
        return keys;
    }

    /**
     * 验证用户和部门是否存在
     */
    private void validateUserAndDepartment(Long userId, Long departmentId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted()) {
            throw new BusinessException.DataNotFoundException("用户不存在");
        }

        SysDepartment department = sysDepartmentMapper.selectById(departmentId);
        if (department == null || department.getDeleted()) {
            throw new BusinessException.DataNotFoundException("部门不存在");
        }
    }

    /**
     * 删除相关缓存
     */
    private void invalidateCache(Long userId, Long departmentId) {
        String userKey = RedisConstants.USER_DEPARTMENTS_KEY + userId;
        String depKey = RedisConstants.DEPARTMENT_USERS_KEY + departmentId;
        cacheUtil.deleteAll(List.of(userKey, depKey));
    }
}
