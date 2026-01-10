package com.universe.life.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.message.domain.po.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话列表 Mapper
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
