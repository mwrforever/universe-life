package com.universe.life.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.message.domain.po.PublicRoom;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公共聊天室 Mapper
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Mapper
public interface PublicRoomMapper extends BaseMapper<PublicRoom> {
}
