package com.universe.life.task.privacy.mapstruct;

import com.universe.life.task.privacy.domain.dto.TaskAcceptanceDTO;
import com.universe.life.task.privacy.domain.vo.TaskAcceptanceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 任务接受记录对象转换器
 *
 * @author universe-life
 */
@Mapper(componentModel = "spring", uses = JsonConvertMapstruct.class)
public interface TaskAcceptanceMapstruct {

    /**
     * DTO转换为VO
     *
     * @param dto 接受记录DTO
     * @return 接受记录VO
     */
    @Mapping(target = "submitImages", source = "submitImagesJson", qualifiedByName = "jsonToStringList")
    TaskAcceptanceVO toVO(TaskAcceptanceDTO dto);

    /**
     * DTO列表转换为VO列表
     *
     * @param dtoList DTO列表
     * @return VO列表
     */
    List<TaskAcceptanceVO> toVOList(List<TaskAcceptanceDTO> dtoList);
}
