package com.universe.life.user.privacy.mapstruct;

import com.universe.life.user.privacy.domain.dto.request.RoleCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.RoleUpdateRequest;
import com.universe.life.user.privacy.domain.po.Resource;
import com.universe.life.user.privacy.domain.po.Role;
import com.universe.life.user.privacy.domain.vo.ResourceSimpleVO;
import com.universe.life.user.privacy.domain.vo.RoleDetailVO;
import com.universe.life.user.privacy.domain.vo.RoleListVO;
import com.universe.life.user.privacy.domain.vo.RoleOptionVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @author 毛伟然
 * @since 2025/12/8 16:18
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapstruct {

    /**
     * 创建角色
     *
     * @param request 创建角色请求参数
     * @return 角色信息
     */
    Role toPo(RoleCreateRequest request);

    /**
     * 转换为角色详情VO
     *
     * @param role 角色信息
     * @return 角色详情VO
     */
    RoleDetailVO toDetailVO(Role role);

    /**
     * 更新角色
     *
     * @param request 更新角色请求参数
     * @return 角色信息
     */
    Role toPo(RoleUpdateRequest request);

    /**
     * 角色列表转换为角色列表VO
     *
     * @param records 角色列表
     * @return 角色列表VO
     */
    List<RoleListVO> toListVO(List<Role> records);

    /**
     * 资源列表转换为资源列表VO
     *
     * @param resources 资源列表
     * @return 资源列表VO
     */
    List<ResourceSimpleVO> toSimpleVO(List<Resource> resources);

    /**
     * 角色列表转换为角色选项列表VO
     *
     * @param roles 角色列表
     * @return 角色选项列表VO
     */
    List<RoleOptionVO> toOptionVOList(List<Role> roles);
}
