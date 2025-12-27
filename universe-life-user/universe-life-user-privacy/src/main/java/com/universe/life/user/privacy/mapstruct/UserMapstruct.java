package com.universe.life.user.privacy.mapstruct;

import com.universe.life.common.mapstruct.BaseMapstruct;
import com.universe.life.model.enums.UserStatus;
import com.universe.life.user.privacy.domain.dto.AdminUserDetailDTO;
import com.universe.life.user.privacy.domain.dto.AdminUserRoleListDTO;
import com.universe.life.user.privacy.domain.dto.request.UserCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.UserUpdateRequest;
import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.domain.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 用户对象转换器
 *
 * @author Claude
 * @since 2025-12-02
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapstruct extends BaseMapstruct {

    /**
     * DTO 转 AdminUserDetailVO
     */
    AdminUserDetailVO toAdminUserDetailVO(AdminUserDetailDTO dto);

    /**
     * DTO 转 AdminUserListVO
     */
    AdminUserListVO toAdminUserListVO(User po);


    /**
     * PO 转 AdminUserDetailDTO
     */
    AdminUserDetailDTO toAdminUserDetailDTO(User po);


    /**
     * CreateRequest 转 PO
     */
    User toPO(UserCreateRequest request);

    /**
     * UserStatus 转 UserStatusVO
     */
    @Named("toUserStatusVO")
    default UserStatusVO toUserStatusVO(UserStatus status) {
        UserStatusVO vo = new UserStatusVO();
        if (status != null) {
            vo.setStatus(status);
        }
        return vo;
    }

    /**
     * DTO 转 AdminUserRoleVO
     */
    AdminUserRoleVO toAdminUserRoleVO(AdminUserRoleListDTO c);

    /**
     * UserUpdateRequest 转 PO
     */
    User toPoByUserUpdateRequest(UserUpdateRequest request);

    /**
     * UserUpdateRequest 转 AdminUserUpdateVO
     */
    AdminUserUpdateVO toAdminUserUpdateVO(UserUpdateRequest request);

    /**
     * PO 转 UserInfoVO（用户端）
     */
    UserInfoVO toUserInfoVO(User po);
}