package com.universe.life.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.message.domain.po.ChatGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 群组 Mapper
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {
}
