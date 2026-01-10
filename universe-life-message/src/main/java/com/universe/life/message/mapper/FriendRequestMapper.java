package com.universe.life.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.message.domain.po.FriendRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友申请 Mapper
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
}
