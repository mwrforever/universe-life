package com.universe.life.message.service;

import com.universe.life.message.domain.dto.request.CreateGroupRequest;
import com.universe.life.message.domain.vo.GroupMemberVO;
import com.universe.life.message.domain.vo.GroupVO;

import java.util.List;

/**
 * 群组服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface GroupService {

    /**
     * 创建群组
     *
     * @param request   创建请求
     * @param creatorId 创建者ID
     * @return 群组ID
     */
    Long createGroup(CreateGroupRequest request, Long creatorId);

    /**
     * 邀请用户入群
     *
     * @param groupId   群组ID
     * @param inviterId 邀请人ID
     * @param userIds   被邀请用户ID列表
     */
    void inviteToGroup(Long groupId, Long inviterId, List<Long> userIds);

    /**
     * 通过群聊码加入群组
     *
     * @param groupCode 群聊码
     * @param userId    用户ID
     */
    void joinByCode(String groupCode, Long userId);

    /**
     * 移除群成员
     *
     * @param groupId    群组ID
     * @param operatorId 操作人ID
     * @param memberId   被移除成员ID
     */
    void removeMember(Long groupId, Long operatorId, Long memberId);

    /**
     * 主动退出群组
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     */
    void leaveGroup(Long groupId, Long userId);

    /**
     * 转让群主
     *
     * @param groupId        群组ID
     * @param currentOwnerId 当前群主ID
     * @param newOwnerId     新群主ID
     */
    void transferOwner(Long groupId, Long currentOwnerId, Long newOwnerId);

    /**
     * 解散群组
     *
     * @param groupId 群组ID
     * @param ownerId 群主ID
     */
    void dissolveGroup(Long groupId, Long ownerId);

    /**
     * 获取群组详情
     *
     * @param groupId 群组ID
     * @param userId  当前用户ID
     * @return 群组信息
     */
    GroupVO getGroupDetail(Long groupId, Long userId);

    /**
     * 获取用户加入的群组列表
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<GroupVO> getMyGroups(Long userId);

    /**
     * 获取群组成员列表
     *
     * @param groupId 群组ID
     * @return 成员列表
     */
    List<GroupMemberVO> getGroupMembers(Long groupId);

    /**
     * 获取群组成员ID列表
     *
     * @param groupId 群组ID
     * @return 成员ID列表
     */
    List<Long> getGroupMemberIds(Long groupId);

    /**
     * 检查用户是否在群组中
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 是否在群组中
     */
    boolean isGroupMember(Long groupId, Long userId);

    /**
     * 设置管理员
     *
     * @param groupId    群组ID
     * @param operatorId 操作人ID（群主）
     * @param memberId   成员ID
     * @param isAdmin    是否设为管理员
     */
    void setAdmin(Long groupId, Long operatorId, Long memberId, boolean isAdmin);

    /**
     * 禁言成员
     *
     * @param groupId    群组ID
     * @param operatorId 操作人ID
     * @param memberId   成员ID
     * @param minutes    禁言分钟数，0表示解除禁言，-1表示永久禁言
     */
    void muteMember(Long groupId, Long operatorId, Long memberId, int minutes);

    /**
     * 更新群组信息
     *
     * @param groupId     群组ID
     * @param operatorId  操作人ID
     * @param name        群组名称
     * @param avatar      群组头像
     * @param description 群组描述
     */
    void updateGroupInfo(Long groupId, Long operatorId, String name, String avatar, String description);

    /**
     * 刷新群聊码
     *
     * @param groupId    群组ID
     * @param operatorId 操作人ID
     * @return 新的群聊码
     */
    String refreshGroupCode(Long groupId, Long operatorId);
}
