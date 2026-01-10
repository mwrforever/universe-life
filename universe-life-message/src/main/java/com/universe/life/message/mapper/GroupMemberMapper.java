package com.universe.life.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.message.domain.po.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组成员 Mapper
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    /**
     * 查询群组的成员ID列表
     *
     * @param groupId 群组ID
     * @return 成员ID列表
     */
    List<Long> selectMemberIdsByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询用户加入的群组ID列表
     *
     * @param userId 用户ID
     * @return 群组ID列表
     */
    List<Long> selectGroupIdsByUserId(@Param("userId") Long userId);
}
