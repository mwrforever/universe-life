package com.universe.life.user.privacy.mapstruct;

import com.universe.life.common.mapstruct.BaseMapstruct;
import com.universe.life.user.privacy.domain.dto.request.UserAuthCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.UserAuthRequest;
import com.universe.life.user.privacy.domain.po.UserAuth;
import com.universe.life.user.privacy.domain.vo.UserAuthCreateVO;
import com.universe.life.user.privacy.domain.vo.UserAuthListVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 用户认证对象转换器
 *
 * @author Claude
 * @since 2025-12-02
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAuthMapstruct extends BaseMapstruct {

    /**
     * PO 转 UserAuthCreateVO
     */
    UserAuthCreateVO toUserAuthCreateVO(UserAuth po);

    /**
     * PO 列表转 UserAuthListVO 列表
     */
    List<UserAuthListVO> toUserAuthListVOList(List<UserAuth> pos);

    
    /**
     * CreateRequest 转 PO
     */
    UserAuth toPO(UserAuthCreateRequest request);

    /**
     * UserAuthRequest 转 PO
     */
    UserAuth userAuthRequestToPo(UserAuthRequest request);

}