package com.universe.life.user.privacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.universe.life.common.result.PageResult;
import com.universe.life.user.privacy.domain.dao.query.SysDepartmentListQuery;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentStatusUpdateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentUpdateRequest;
import com.universe.life.user.privacy.domain.po.SysDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentDetailVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentListVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentTreeVO;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
public interface ISysDepartmentService extends IService<SysDepartment> {

    /**
     * 创建部门
     *
     * @param request 创建请求
     * @return 部门详情
     */
    SysDepartmentDetailVO createDepartment(SysDepartmentCreateRequest request);

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    SysDepartmentDetailVO getDepartmentById(Long id);

    /**
     * 更新部门信息
     *
     * @param id      部门ID
     * @param request 更新请求
     * @return 部门详情
     */
    SysDepartmentDetailVO updateDepartment(Long id, SysDepartmentUpdateRequest request);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDepartment(Long id);

    /**
     * 分页查询部门列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SysDepartmentListVO> pageDepartments(SysDepartmentListQuery query);

    /**
     * 更新部门状态
     *
     * @param id      部门ID
     * @param request 状态更新请求
     */
    void updateDepartmentStatus(Long id, SysDepartmentStatusUpdateRequest request);

    /**
     * 获取部门树形结构
     *
     * @return 部门树
     */
    List<SysDepartmentTreeVO> getDepartmentTree();

    /**
     * 获取所有启用的部门选项
     *
     * @return 部门选项列表
     */
    List<SysDepartmentSimpleVO> getDepartmentOptions();
}
