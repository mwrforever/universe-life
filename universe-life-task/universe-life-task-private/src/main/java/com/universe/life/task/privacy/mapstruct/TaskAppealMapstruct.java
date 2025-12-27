package com.universe.life.task.privacy.mapstruct;

import com.universe.life.task.privacy.domain.dto.TaskAppealDTO;
import com.universe.life.task.privacy.domain.vo.TaskAppealVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 任务申诉对象转换器
 *
 * @author universe-life
 */
@Mapper(componentModel = "spring", uses = JsonConvertMapstruct.class)
public interface TaskAppealMapstruct {

    /**
     * DTO转换为VO
     *
     * @param dto 申诉DTO
     * @return 申诉VO
     */
    @Mapping(target = "evidenceImages", source = "evidenceImagesJson", qualifiedByName = "jsonToStringList")
    TaskAppealVO toVO(TaskAppealDTO dto);

    /**
     * DTO列表转换为VO列表
     *
     * @param dtoList DTO列表
     * @return VO列表
     */
    List<TaskAppealVO> toVOList(List<TaskAppealDTO> dtoList);
}
