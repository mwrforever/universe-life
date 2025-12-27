package com.universe.life.user.privacy.mapstruct;

import com.universe.life.model.domain.dto.UserInfoDTO;
import com.universe.life.user.privacy.domain.dto.request.SysUserCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.SysUserUpdateRequest;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.vo.SysUserDetailVO;
import com.universe.life.user.privacy.domain.vo.SysUserListVO;
import com.universe.life.user.privacy.domain.vo.SysUserOptionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 平台员工对象转换器
 *
 * @author 毛伟然
 * @since 2025-12-11
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysUserMapstruct {

    /**
     * 创建请求转PO
     *
     * @param request 创建请求
     * @return PO对象
     */
    @Mapping(target = "password", ignore = true)
    SysUser toPo(SysUserCreateRequest request);

    /**
     * 更新请求转PO
     *
     * @param request 更新请求
     * @return PO对象
     */
    SysUser toPo(SysUserUpdateRequest request);

    /**
     * PO转详情VO
     *
     * @param sysUser PO对象
     * @return 详情VO
     */
    SysUserDetailVO toDetailVO(SysUser sysUser);

    /**
     * PO列表转列表VO
     *
     * @param sysUsers PO列表
     * @return 列表VO
     */
    List<SysUserListVO> toListVO(List<SysUser> sysUsers);

    /**
     * PO列表转选项VO列表
     *
     * @param sysUsers PO列表
     * @return 选项VO列表
     */
    List<SysUserOptionVO> toOptionVOList(List<SysUser> sysUsers);

    /**
     * PO转用户信息DTO
     *
     * @param sysUser PO对象
     * @return 用户信息DTO
     */
    @Mapping(target = "username", source = "employeeNo")
    UserInfoDTO toUserInfoDTO(SysUser sysUser);
}
