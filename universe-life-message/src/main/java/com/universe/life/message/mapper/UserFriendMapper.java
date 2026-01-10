package com.universe.life.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.message.domain.po.UserFriend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友关系 Mapper
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Mapper
public interface UserFriendMapper extends BaseMapper<UserFriend> {

    /**
     * 查询用户的好友ID列表
     *
     * @param userId 用户ID
     * @return 好友ID列表
     */
    List<Long> selectFriendIdsByUserId(@Param("userId") Long userId);
}
