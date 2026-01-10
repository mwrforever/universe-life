package com.universe.life.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.message.domain.po.UserChatSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户聊天设置 Mapper
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Mapper
public interface UserChatSettingMapper extends BaseMapper<UserChatSetting> {
}
