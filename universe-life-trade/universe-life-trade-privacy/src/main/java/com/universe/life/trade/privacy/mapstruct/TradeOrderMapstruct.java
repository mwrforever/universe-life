package com.universe.life.trade.privacy.mapstruct;

import com.universe.life.trade.privacy.domain.dto.TradeOrderDTO;
import com.universe.life.trade.privacy.domain.vo.TradeOrderVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 交易订单对象转换器
 *
 * @author universe-life
 */
@Mapper(componentModel = "spring")
public interface TradeOrderMapstruct {

    @Mapping(target = "submitImages", source = "submitImagesJson", qualifiedByName = "jsonToList")
    TradeOrderVO toVO(TradeOrderDTO dto);

    List<TradeOrderVO> toVOList(List<TradeOrderDTO> dtoList);

    @Named("jsonToList")
    default List<String> jsonToList(String json) {
        return JsonConvertMapstruct.jsonToStringList(json);
    }
}
