package com.universe.life.user.privacy.mapstruct;

import com.universe.life.common.mapstruct.BaseMapstruct;
import com.universe.life.user.privacy.domain.dto.request.UserDetailUpdateRequest;
import com.universe.life.user.privacy.domain.po.UserDetail;
import com.universe.life.user.privacy.domain.vo.UserDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 用户详情对象转换器
 *
 * @author Claude
 * @since 2025-12-02
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserDetailMapstruct extends BaseMapstruct {

    /**
     * PO 转 VO
     */
    UserDetailVO toVO(UserDetail po);

    /**
     * UpdateRequest 转 PO
     */
    UserDetail toPO(UserDetailUpdateRequest request);

}