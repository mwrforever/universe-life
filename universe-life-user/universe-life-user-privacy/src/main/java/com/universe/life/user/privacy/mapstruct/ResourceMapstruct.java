package com.universe.life.user.privacy.mapstruct;

import com.universe.life.user.privacy.domain.dto.request.ResourceCreateRequest;
import com.universe.life.user.privacy.domain.dto.request.ResourceUpdateRequest;
import com.universe.life.user.privacy.domain.po.Resource;
import com.universe.life.user.privacy.domain.vo.ResourceDetailVO;
import com.universe.life.user.privacy.domain.vo.ResourceListVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @author 毛伟然
 * @since 2025/12/8 16:16
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResourceMapstruct {
    Resource toPo(ResourceCreateRequest request);

    ResourceDetailVO toDetailVO(Resource resource);

    Resource toPo(ResourceUpdateRequest request);

    List<ResourceListVO> toListVO(List<Resource> resource);
}
