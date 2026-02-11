package com.universe.life.message.property;

import com.universe.life.message.domain.dto.request.CreateGroupRequest;
import com.universe.life.message.domain.po.ChatGroup;
import com.universe.life.message.domain.po.GroupMember;
import com.universe.life.message.domain.vo.GroupVO;
import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.enums.GroupMemberRole;
import com.universe.life.message.enums.GroupStatus;
import com.universe.life.message.mapper.ChatGroupMapper;
import com.universe.life.message.mapper.GroupMemberMapper;
import com.universe.life.message.service.GroupService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 群组服务属性测试
 * 使用 jqwik 进行属性测试，验证群组管理的正确性
 *
 * Feature: distributed-chat-system
 * 
 * @author Kiro
 * @since 2026/01/09
 */
@SpringBootTest
@ActiveProfiles("test")
public class GroupServicePropertyTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private ChatGroupMapper chatGroupMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    /**
     * Property 12: 群组创建者权限
     * *For any* newly created group, the creator must be the owner with OWNER role
     * 
     * **Validates: Requirements 7.1**
     */
    @Property(tries = 100)
    @Label("Property 12: 群组创建者权限")
    void groupCreatorIsOwner(
            @ForAll @LongRange(min = 1, max = 10000) Long creatorId,
            @ForAll @StringLength(min = 1, max = 50) String groupName
    ) {
        // Given: 创建群组请求
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName(groupName);
        request.setDescription("Test group");

        // When: 创建群组
        Long groupId = groupService.createGroup(request, creatorId);

        // Then: 创建者应该是群主
        GroupVO groupDetail = groupService.getGroupDetail(groupId, creatorId);
        assertEquals(creatorId, groupDetail.getOwnerId(), "创建者应该是群主");
        assertEquals(GroupMemberRole.OWNER, groupDetail.getMyRole(), "创建者角色应该是 OWNER");

        // 验证成员数为1
        assertEquals(1, groupDetail.getMemberCount(), "初始成员数应该为1");

        // Cleanup
        cleanupGroup(groupId);
    }

    /**
     * Property 13: 群组邀请权限验证
     * *For any* group with allowMemberInvite=false, only owner/admin can invite
     * 
     * **Validates: Requirements 7.3, 7.4, 7.5**
     */
    @Property(tries = 50)
    @Label("Property 13: 群组邀请权限验证")
    void groupInvitePermissionIsCorrect(
            @ForAll @LongRange(min = 1, max = 10000) Long ownerId,
            @ForAll @LongRange(min = 1, max = 10000) Long memberId,
            @ForAll @LongRange(min = 1, max = 10000) Long inviteeId
    ) {
        Assume.that(!ownerId.equals(memberId));
        Assume.that(!ownerId.equals(inviteeId));
        Assume.that(!memberId.equals(inviteeId));

        // Given: 创建一个不允许成员邀请的群组
        Long groupId = createGroupWithSettings(ownerId, "Test Group", false);
        
        // 添加一个普通成员
        addMemberToGroup(groupId, memberId, GroupMemberRole.MEMBER, ownerId);

        // Then: 群主可以邀请
        assertTrue(canInvite(groupId, ownerId), "群主应该可以邀请");
        
        // 普通成员不能邀请（因为 allowMemberInvite=false）
        assertFalse(canInviteAsMember(groupId, memberId), "普通成员不应该可以邀请");

        // Cleanup
        cleanupGroup(groupId);
    }

    /**
     * Property 14: 群聊码加入验证
     * *For any* valid group code, joining should add the user as a MEMBER
     * 
     * **Validates: Requirements 7.6**
     */
    @Property(tries = 50)
    @Label("Property 14: 群聊码加入验证")
    void groupCodeJoinAddsAsMember(
            @ForAll @LongRange(min = 1, max = 10000) Long ownerId,
            @ForAll @LongRange(min = 1, max = 10000) Long joinerId
    ) {
        Assume.that(!ownerId.equals(joinerId));

        // Given: 创建群组并获取群聊码
        Long groupId = createGroupWithSettings(ownerId, "Test Group", true);
        ChatGroup group = chatGroupMapper.selectById(groupId);
        String groupCode = group.getGroupCode();

        // When: 通过群聊码加入
        groupService.joinByCode(groupCode, joinerId);

        // Then: 加入者应该是普通成员
        assertTrue(groupService.isGroupMember(groupId, joinerId), "应该成为群成员");
        
        GroupMember member = getMemberByGroupAndUser(groupId, joinerId);
        assertNotNull(member, "成员记录应该存在");
        assertEquals(GroupMemberRole.MEMBER, member.getRole(), "角色应该是 MEMBER");

        // Cleanup
        cleanupGroup(groupId);
    }

    /**
     * Property: 群组成员数正确性
     * *For any* group, memberCount should equal the actual number of members
     * 
     * **Validates: Requirements 7.9**
     */
    @Property(tries = 50)
    @Label("群组成员数正确性")
    void groupMemberCountIsAccurate(
            @ForAll @LongRange(min = 1, max = 10000) Long ownerId,
            @ForAll @LongRange(min = 1, max = 5) int additionalMembers
    ) {
        // Given: 创建群组
        Long groupId = createGroupWithSettings(ownerId, "Test Group", true);

        // 添加额外成员
        for (int i = 0; i < additionalMembers; i++) {
            Long memberId = ownerId + i + 1;
            addMemberToGroup(groupId, memberId, GroupMemberRole.MEMBER, ownerId);
        }

        // When: 获取群组详情
        GroupVO groupDetail = groupService.getGroupDetail(groupId, ownerId);

        // Then: 成员数应该正确
        int expectedCount = 1 + additionalMembers; // owner + additional members
        List<Long> actualMembers = groupService.getGroupMemberIds(groupId);
        
        assertEquals(expectedCount, actualMembers.size(), "实际成员数应该正确");

        // Cleanup
        cleanupGroup(groupId);
    }

    /**
     * Property: 群主转让后权限正确
     * *For any* ownership transfer, the new owner should have OWNER role and old owner should have MEMBER role
     * 
     * **Validates: Requirements 7.7**
     */
    @Property(tries = 50)
    @Label("群主转让后权限正确")
    void ownershipTransferIsCorrect(
            @ForAll @LongRange(min = 1, max = 10000) Long originalOwnerId,
            @ForAll @LongRange(min = 1, max = 10000) Long newOwnerId
    ) {
        Assume.that(!originalOwnerId.equals(newOwnerId));

        // Given: 创建群组并添加新群主候选人
        Long groupId = createGroupWithSettings(originalOwnerId, "Test Group", true);
        addMemberToGroup(groupId, newOwnerId, GroupMemberRole.MEMBER, originalOwnerId);

        // When: 转让群主
        groupService.transferOwner(groupId, originalOwnerId, newOwnerId);

        // Then: 验证权限变化
        GroupMember oldOwnerMember = getMemberByGroupAndUser(groupId, originalOwnerId);
        GroupMember newOwnerMember = getMemberByGroupAndUser(groupId, newOwnerId);

        assertEquals(GroupMemberRole.MEMBER, oldOwnerMember.getRole(), "原群主应该变成普通成员");
        assertEquals(GroupMemberRole.OWNER, newOwnerMember.getRole(), "新群主应该是 OWNER");

        // 验证群组的 ownerId 也更新了
        ChatGroup group = chatGroupMapper.selectById(groupId);
        assertEquals(newOwnerId, group.getOwnerId(), "群组的 ownerId 应该更新");

        // Cleanup
        cleanupGroup(groupId);
    }

    // ==================== Helper Methods ====================

    private Long createGroupWithSettings(Long ownerId, String name, boolean allowMemberInvite) {
        ChatGroup group = ChatGroup.builder()
                .name(name)
                .description("Test group")
                .ownerId(ownerId)
                .groupCode(generateGroupCode())
                .isPublic(BooleanFlag.NO)
                .allowMemberInvite(allowMemberInvite ? BooleanFlag.YES : BooleanFlag.NO)
                .maxMembers(500)
                .memberCount(1)
                .status(GroupStatus.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        chatGroupMapper.insert(group);

        // 添加群主为成员
        GroupMember ownerMember = GroupMember.builder()
                .groupId(group.getId())
                .userId(ownerId)
                .role(GroupMemberRole.OWNER)
                .muted(BooleanFlag.NO)
                .joinedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        groupMemberMapper.insert(ownerMember);

        return group.getId();
    }

    private void addMemberToGroup(Long groupId, Long userId, GroupMemberRole role, Long inviterId) {
        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role(role)
                .muted(BooleanFlag.NO)
                .inviterId(inviterId)
                .joinedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        groupMemberMapper.insert(member);

        // 更新成员数
        ChatGroup group = chatGroupMapper.selectById(groupId);
        group.setMemberCount(group.getMemberCount() + 1);
        chatGroupMapper.updateById(group);
    }

    private GroupMember getMemberByGroupAndUser(Long groupId, Long userId) {
        return groupMemberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId)
        );
    }

    private boolean canInvite(Long groupId, Long userId) {
        GroupMember member = getMemberByGroupAndUser(groupId, userId);
        if (member == null) return false;
        return member.getRole().isOwner() || member.getRole().isAdmin();
    }

    private boolean canInviteAsMember(Long groupId, Long userId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        GroupMember member = getMemberByGroupAndUser(groupId, userId);
        if (member == null) return false;
        
        // 如果允许成员邀请，或者是管理员/群主，则可以邀请
        if (group.getAllowMemberInvite().isYes()) return true;
        return member.getRole().isOwner() || member.getRole().isAdmin();
    }

    private String generateGroupCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private void cleanupGroup(Long groupId) {
        // 删除所有成员
        groupMemberMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
        );
        // 删除群组
        chatGroupMapper.deleteById(groupId);
    }
}
