package com.universe.life.user.privacy.mapstruct;

import com.universe.life.user.privacy.domain.dto.request.SysDepartmentCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysDepartmentUpdateRequest;
import com.universe.life.user.privacy.domain.po.SysDepartment;
import com.universe.life.user.privacy.domain.vo.SysDepartmentDetailVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentListVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentSimpleVO;
import com.universe.life.user.privacy.domain.vo.SysDepartmentTreeVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 部门对象转换器
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysDepartmentMapstruct {

    /**
     * 创建请求转PO
     *
     * @param request 创建请求
     * @return PO对象
     */
    SysDepartment toPo(SysDepartmentCreateRequest request);

    /**
     * 更新请求转PO
     *
     * @param request 更新请求
     * @return PO对象
     */
    SysDepartment toPo(SysDepartmentUpdateRequest request);

    /**
     * PO转详情VO
     *
     * @param department PO对象
     * @return 详情VO
     */
    SysDepartmentDetailVO toDetailVO(SysDepartment department);

    /**
     * PO列表转列表VO
     *
     * @param departments PO列表
     * @return 列表VO
     */
    List<SysDepartmentListVO> toListVO(List<SysDepartment> departments);

    /**
     * PO列表转树形VO列表
     *
     * @param departments PO列表
     * @return 树形VO列表
     */
    List<SysDepartmentTreeVO> toTreeVOList(List<SysDepartment> departments);

    /**
     * PO列表转简单VO列表
     *
     * @param departments PO列表
     * @return 简单VO列表
     */
    List<SysDepartmentSimpleVO> toSimpleVOList(List<SysDepartment> departments);
}
