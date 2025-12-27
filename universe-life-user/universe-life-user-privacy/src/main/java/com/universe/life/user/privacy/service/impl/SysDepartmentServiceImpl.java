package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.common.result.PageResult;
import com.universe.life.user.privacy.domain.dao.query.SysDepartmentListQuery;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentUpdateRequest;
import com.universe.life.user.privacy.domain.po.SysDepartment;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.po.SysUserDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentDetailVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentListVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentTreeVO;
import com.universe.life.user.privacy.enums.CommonStatus;
import com.universe.life.user.privacy.mapper.SysDepartmentMapper;
import com.universe.life.user.privacy.mapstruct.SysDepartmentMapstruct;
import com.universe.life.user.privacy.service.ISysDepartmentService;
import com.universe.life.user.privacy.service.ISysUserDepartmentService;
import com.universe.life.user.privacy.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Slf4j
@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements ISysDepartmentService {

    private final SysDepartmentMapstruct departmentMapstruct;

    private final ISysUserDepartmentService userDepartmentService;

    private ISysUserService sysUserService;

    public SysDepartmentServiceImpl(SysDepartmentMapstruct departmentMapstruct,
                                    ISysUserDepartmentService userDepartmentService) {
        this.departmentMapstruct = departmentMapstruct;
        this.userDepartmentService = userDepartmentService;
    }

    @Lazy
    @Autowired
    public void setSysUserService(ISysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDepartmentDetailVO createDepartment(SysDepartmentCreateRequest request) {
        log.info("创建部门，部门编码：{}", request.getDeptCode());

        // 检查部门编码是否已存在
        boolean existsDeptCode = lambdaQuery()
                .eq(SysDepartment::getDeptCode, request.getDeptCode())
                .exists();
        if (existsDeptCode) {
            throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.Formatter.dataAlreadyExist("部门编码"));
        }

        // 检查父部门是否存在
        if (request.getParentId() != null && request.getParentId() > 0) {
            boolean existsParent = lambdaQuery()
                    .eq(SysDepartment::getId, request.getParentId())
                    .exists();
            if (!existsParent) {
                throw new BusinessException.DataNotFoundException(ExceptionMessage.Formatter.dataNotFound("父部门"));
            }
        }

        // 创建部门
        SysDepartment department = departmentMapstruct.toPo(request);
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        boolean saved = save(department);
        if (!saved) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("部门新增"));
        }

        log.info("创建部门成功，部门ID：{}", department.getId());
        return getDepartmentById(department.getId());
    }

    @Override
    public SysDepartmentDetailVO getDepartmentById(Long id) {
        SysDepartment department = getById(id);
        if (ObjectUtil.isNull(department)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        SysDepartmentDetailVO detailVO = departmentMapstruct.toDetailVO(department);

        // 查询父部门名称
        if (department.getParentId() != null && department.getParentId() > 0) {
            SysDepartment parentDept = getById(department.getParentId());
            if (parentDept != null) {
                detailVO.setParentName(parentDept.getDeptName());
            }
        }

        // 查询负责人姓名
        if (department.getLeaderId() != null) {
            SysUser leader = sysUserService.getById(department.getLeaderId());
            if (leader != null) {
                detailVO.setLeaderName(leader.getRealName());
            }
        }

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDepartmentDetailVO updateDepartment(Long id, SysDepartmentUpdateRequest request) {
        log.info("更新部门，部门ID：{}", id);

        SysDepartment existingDepartment = getById(id);
        if (ObjectUtil.isNull(existingDepartment)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 检查父部门不能是自己或自己的子部门
        if (request.getParentId() != null && request.getParentId() > 0) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException.OperationNotAllowedException(ExceptionMessage.Formatter.operationFailed("父部门不能是自己"));
            }
            // 检查是否是子部门
            if (isChildDepartment(id, request.getParentId())) {
                throw new BusinessException.OperationNotAllowedException(ExceptionMessage.Formatter.operationFailed("父部门不能是自己的子部门"));
            }
        }

        // 更新部门
        SysDepartment department = departmentMapstruct.toPo(request);
        department.setId(id);
        boolean updated = updateById(department);
        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("部门更新"));
        }

        log.info("更新部门成功，部门ID：{}", id);
        return getDepartmentById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id) {
        log.info("删除部门，部门ID：{}", id);

        SysDepartment department = getById(id);
        if (ObjectUtil.isNull(department)) {
            throw new BusinessException.DataNotFoundException(ExceptionMessage.DATA_NOT_FOUND);
        }

        // 检查是否有子部门
        boolean hasChildren = lambdaQuery()
                .eq(SysDepartment::getParentId, id)
                .exists();
        if (hasChildren) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.Formatter.operationFailed("该部门下存在子部门，不允许删除"));
        }

        // 检查是否有员工关联
        Long userCount = userDepartmentService.lambdaQuery()
                .eq(SysUserDepartment::getDepartmentId, id)
                .count();
        if (userCount > 0) {
            throw new BusinessException.OperationNotAllowedException(ExceptionMessage.Formatter.operationFailed("该部门下存在员工，不允许删除"));
        }

        // 软删除部门
        removeById(id);

        log.info("删除部门成功，部门ID：{}", id);
    }

    @Override
    public PageResult<SysDepartmentListVO> pageDepartments(SysDepartmentListQuery query) {
        IPage<SysDepartmentListVO> page = new Page<>(query.getPage(), query.getSize());
        IPage<SysDepartmentListVO> result = baseMapper.selectDepartmentList(page, query);

        if (CollUtil.isEmpty(result.getRecords())) {
            return PageResult.empty(page);
        }

        return PageResult.of(result.getRecords(), page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartmentStatus(Long id, SysDepartmentStatusUpdateRequest request) {
        log.info("更新部门状态，部门ID：{}，状态：{}", id, request.getStatus());

        boolean updated = lambdaUpdate()
                .set(SysDepartment::getStatus, request.getStatus())
                .eq(SysDepartment::getId, id)
                .update();

        if (!updated) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("部门状态更新"));
        }

        log.info("更新部门状态成功，部门ID：{}", id);
    }

    @Override
    public List<SysDepartmentTreeVO> getDepartmentTree() {
        List<SysDepartment> allDepartments = lambdaQuery()
                .eq(SysDepartment::getStatus, CommonStatus.ENABLE)
                .orderByAsc(SysDepartment::getSortOrder)
                .list();

        if (CollUtil.isEmpty(allDepartments)) {
            return new ArrayList<>();
        }

        List<SysDepartmentTreeVO> treeVOList = departmentMapstruct.toTreeVOList(allDepartments);

        // 构建树形结构
        return buildTree(treeVOList);
    }

    @Override
    public List<SysDepartmentSimpleVO> getDepartmentOptions() {
        List<SysDepartment> departments = lambdaQuery()
                .select(SysDepartment::getId, SysDepartment::getDeptCode, SysDepartment::getDeptName)
                .eq(SysDepartment::getStatus, CommonStatus.ENABLE)
                .orderByAsc(SysDepartment::getSortOrder)
                .list();

        if (CollUtil.isEmpty(departments)) {
            return new ArrayList<>();
        }

        return departmentMapstruct.toSimpleVOList(departments);
    }

    /**
     * 判断targetId是否是parentId的子部门
     */
    private boolean isChildDepartment(Long parentId, Long targetId) {
        List<SysDepartment> children = lambdaQuery()
                .eq(SysDepartment::getParentId, parentId)
                .list();

        for (SysDepartment child : children) {
            if (child.getId().equals(targetId)) {
                return true;
            }
            if (isChildDepartment(child.getId(), targetId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建树形结构
     */
    private List<SysDepartmentTreeVO> buildTree(List<SysDepartmentTreeVO> allNodes) {
        Map<Long, List<SysDepartmentTreeVO>> parentIdMap = allNodes.stream()
                .collect(Collectors.groupingBy(SysDepartmentTreeVO::getParentId));

        allNodes.forEach(node -> {
            List<SysDepartmentTreeVO> children = parentIdMap.get(node.getId());
            node.setChildren(children != null ? children : new ArrayList<>());
        });

        return allNodes.stream()
                .filter(node -> node.getParentId() == null || node.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
