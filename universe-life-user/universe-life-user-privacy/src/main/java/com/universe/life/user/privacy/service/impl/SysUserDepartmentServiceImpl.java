package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.user.privacy.domain.po.SysUserDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.mapper.SysUserDepartmentMapper;
import com.universe.life.user.privacy.service.ISysUserDepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        List<SysUserDepartment> userDepartments = new ArrayList<>();
        for (Long departmentId : departmentIds) {
            SysUserDepartment userDepartment = new SysUserDepartment();
            userDepartment.setUserId(userId);
            userDepartment.setDepartmentId(departmentId);
            // 设置是否为主部门
            userDepartment.setIsPrimary(departmentId.equals(primaryDepartmentId));
            userDepartments.add(userDepartment);
        }

        // 如果没有指定主部门，默认第一个为主部门
        if (primaryDepartmentId == null && !userDepartments.isEmpty()) {
            userDepartments.get(0).setIsPrimary(true);
        }

        saveBatch(userDepartments);

        log.info("分配用户部门成功，用户ID：{}", userId);
    }

    @Override
    public List<SysDepartmentSimpleVO> getUserDepartments(Long userId) {
        return baseMapper.selectDepartmentsByUserId(userId);
    }

    @Override
    public SysDepartmentSimpleVO getUserPrimaryDepartment(Long userId) {
        return baseMapper.selectPrimaryDepartmentByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserDepartments(Long userId) {
        remove(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getUserId, userId));
    }
}
