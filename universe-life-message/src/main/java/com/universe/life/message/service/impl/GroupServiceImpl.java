package com.universe.life.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.message.domain.dto.request.CreateGroupRequest;
import com.universe.life.message.domain.po.ChatGroup;
import com.universe.life.message.domain.po.GroupMember;
import com.universe.life.message.domain.vo.GroupMemberVO;
import com.universe.life.message.domain.vo.GroupVO;
import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.enums.GroupMemberRole;
import com.universe.life.message.enums.GroupStatus;
import com.universe.life.message.exception.ChatExceptionMessage;
import com.universe.life.message.mapper.ChatGroupMapper;
import com.universe.life.message.mapper.GroupMemberMapper;
import com.universe.life.message.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 群组服务实现类
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private static final int DEFAULT_MAX_MEMBERS = 500;
    private static final int GROUP_CODE_LENGTH = 8;
    private static final String GROUP_MEMBER_CACHE_KEY_PREFIX = "chat:group:members:";
    private static final long GROUP_MEMBER_CACHE_TTL_HOURS = 24;

    private final ChatGroupMapper chatGroupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(CreateGroupRequest request, Long creatorId) {
        // 创建群组
        ChatGroup group = ChatGroup.builder()
                .name(request.getName())
                .avatar(request.getAvatar())
                .description(request.getDescription())
                .ownerId(creatorId)
                .groupCode(generateGroupCode())
                .isPublic(Boolean.TRUE.equals(request.getIsPublic()) ? BooleanFlag.YES : BooleanFlag.NO)
                .allowMemberInvite(request.getAllowMemberInvite() == null || request.getAllowMemberInvite() ? BooleanFlag.YES : BooleanFlag.NO)
                .maxMembers(DEFAULT_MAX_MEMBERS)
                .memberCount(1)
                .status(GroupStatus.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        chatGroupMapper.insert(group);
        Long groupId = group.getId();

        // 添加创建者为群主
        addMember(groupId, creatorId, GroupMemberRole.OWNER, null);

        // 邀请初始成员
        if (!CollectionUtils.isEmpty(request.getMemberIds())) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(creatorId)) {
                    addMember(groupId, memberId, GroupMemberRole.MEMBER, creatorId);
                }
            }
            // 更新成员数
            updateMemberCount(groupId);
        }

        log.info("Group created: id={}, name={}, creator={}", groupId, request.getName(), creatorId);
        return groupId;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteToGroup(Long groupId, Long inviterId, List<Long> userIds) {
        ChatGroup group = getGroupById(groupId);

        // 检查邀请人是否是群成员
        GroupMember inviterMember = getMemberByGroupAndUser(groupId, inviterId);
        if (inviterMember == null) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        // 检查是否允许成员邀请
        if (group.getAllowMemberInvite().isNo() && !inviterMember.getRole().isOwner() && !inviterMember.getRole().isAdmin()) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.MEMBER_INVITE_NOT_ALLOWED);
        }

        // 检查群组是否已满
        int currentCount = group.getMemberCount();
        int toAddCount = 0;

        for (Long userId : userIds) {
            if (!isGroupMember(groupId, userId)) {
                toAddCount++;
            }
        }

        if (currentCount + toAddCount > group.getMaxMembers()) {
            throw new BusinessException.ParamException(ChatExceptionMessage.GROUP_FULL);
        }

        // 添加成员
        for (Long userId : userIds) {
            if (!isGroupMember(groupId, userId)) {
                addMember(groupId, userId, GroupMemberRole.MEMBER, inviterId);
            }
        }

        // 更新成员数
        updateMemberCount(groupId);

        // 清除群成员缓存
        clearGroupMemberCache(groupId);

        log.info("Users invited to group: groupId={}, inviter={}, users={}", groupId, inviterId, userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinByCode(String groupCode, Long userId) {
        // 根据群聊码查找群组
        LambdaQueryWrapper<ChatGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroup::getGroupCode, groupCode)
                .eq(ChatGroup::getStatus, GroupStatus.NORMAL);

        ChatGroup group = chatGroupMapper.selectOne(queryWrapper);
        if (group == null) {
            throw new BusinessException.ParamException(ChatExceptionMessage.INVALID_GROUP_CODE);
        }

        // 检查是否已是成员
        if (isGroupMember(group.getId(), userId)) {
            throw new BusinessException.DataAlreadyExistsException(ChatExceptionMessage.ALREADY_GROUP_MEMBER);
        }

        // 检查群组是否已满
        if (group.getMemberCount() >= group.getMaxMembers()) {
            throw new BusinessException.ParamException(ChatExceptionMessage.GROUP_FULL);
        }

        // 添加成员
        addMember(group.getId(), userId, GroupMemberRole.MEMBER, null);
        updateMemberCount(group.getId());

        // 清除群成员缓存
        clearGroupMemberCache(group.getId());

        log.info("User joined group by code: groupId={}, userId={}", group.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long groupId, Long operatorId, Long memberId) {
        ChatGroup group = getGroupById(groupId);

        // 不能移除群主
        if (group.getOwnerId().equals(memberId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.CANNOT_REMOVE_OWNER);
        }

        // 检查操作人权限
        GroupMember operatorMember = getMemberByGroupAndUser(groupId, operatorId);
        if (operatorMember == null) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        GroupMember targetMember = getMemberByGroupAndUser(groupId, memberId);
        if (targetMember == null) {
            throw new BusinessException.DataNotFoundException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        // 权限校验：群主可移除任何人，管理员只能移除普通成员
        if (!operatorMember.getRole().isOwner()) {
            if (!operatorMember.getRole().isAdmin()) {
                throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_ADMIN);
            }
            if (targetMember.getRole().isAdmin() || targetMember.getRole().isOwner()) {
                throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NO_PERMISSION);
            }
        }

        // 删除成员
        groupMemberMapper.deleteById(targetMember.getId());
        updateMemberCount(groupId);

        // 清除群成员缓存
        clearGroupMemberCache(groupId);

        log.info("Member removed from group: groupId={}, operator={}, member={}", groupId, operatorId, memberId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(Long groupId, Long userId) {
        ChatGroup group = getGroupById(groupId);

        // 群主不能直接退出，需要先转让
        if (group.getOwnerId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_OWNER);
        }

        GroupMember member = getMemberByGroupAndUser(groupId, userId);
        if (member == null) {
            throw new BusinessException.DataNotFoundException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        // 删除成员
        groupMemberMapper.deleteById(member.getId());
        updateMemberCount(groupId);

        // 清除群成员缓存
        clearGroupMemberCache(groupId);

        log.info("User left group: groupId={}, userId={}", groupId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferOwner(Long groupId, Long currentOwnerId, Long newOwnerId) {
        ChatGroup group = getGroupById(groupId);

        // 验证当前群主
        if (!group.getOwnerId().equals(currentOwnerId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_OWNER);
        }

        // 验证新群主是群成员
        GroupMember newOwnerMember = getMemberByGroupAndUser(groupId, newOwnerId);
        if (newOwnerMember == null) {
            throw new BusinessException.DataNotFoundException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        // 更新群组群主
        LambdaUpdateWrapper<ChatGroup> groupUpdate = new LambdaUpdateWrapper<>();
        groupUpdate.eq(ChatGroup::getId, groupId)
                .set(ChatGroup::getOwnerId, newOwnerId)
                .set(ChatGroup::getUpdatedAt, LocalDateTime.now());
        chatGroupMapper.update(null, groupUpdate);

        // 更新原群主角色为普通成员
        LambdaUpdateWrapper<GroupMember> oldOwnerUpdate = new LambdaUpdateWrapper<>();
        oldOwnerUpdate.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, currentOwnerId)
                .set(GroupMember::getRole, GroupMemberRole.MEMBER)
                .set(GroupMember::getUpdatedAt, LocalDateTime.now());
        groupMemberMapper.update(null, oldOwnerUpdate);

        // 更新新群主角色
        LambdaUpdateWrapper<GroupMember> newOwnerUpdate = new LambdaUpdateWrapper<>();
        newOwnerUpdate.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, newOwnerId)
                .set(GroupMember::getRole, GroupMemberRole.OWNER)
                .set(GroupMember::getUpdatedAt, LocalDateTime.now());
        groupMemberMapper.update(null, newOwnerUpdate);

        log.info("Group owner transferred: groupId={}, from={}, to={}", groupId, currentOwnerId, newOwnerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolveGroup(Long groupId, Long ownerId) {
        ChatGroup group = getGroupById(groupId);

        // 验证群主
        if (!group.getOwnerId().equals(ownerId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_OWNER);
        }

        // 更新群组状态为已解散
        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatGroup::getId, groupId)
                .set(ChatGroup::getStatus, GroupStatus.DISSOLVED)
                .set(ChatGroup::getUpdatedAt, LocalDateTime.now());
        chatGroupMapper.update(null, updateWrapper);

        // 删除所有成员
        LambdaQueryWrapper<GroupMember> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(deleteWrapper);

        // 清除群成员缓存
        clearGroupMemberCache(groupId);

        log.info("Group dissolved: groupId={}, owner={}", groupId, ownerId);
    }

    @Override
    public GroupVO getGroupDetail(Long groupId, Long userId) {
        ChatGroup group = getGroupById(groupId);

        // 获取用户在群中的角色
        GroupMember member = getMemberByGroupAndUser(groupId, userId);
        GroupMemberRole myRole = member != null ? member.getRole() : null;

        return GroupVO.builder()
                .id(group.getId())
                .name(group.getName())
                .avatar(group.getAvatar())
                .description(group.getDescription())
                .ownerId(group.getOwnerId())
                .groupCode(group.getGroupCode())
                .isPublic(group.getIsPublic())
                .allowMemberInvite(group.getAllowMemberInvite())
                .maxMembers(group.getMaxMembers())
                .memberCount(group.getMemberCount())
                .myRole(myRole)
                .createdAt(group.getCreatedAt())
                .build();
    }


    @Override
    public List<GroupVO> getMyGroups(Long userId) {
        // 查询用户加入的群组ID
        List<Long> groupIds = groupMemberMapper.selectGroupIdsByUserId(userId);
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }

        // 查询群组信息
        LambdaQueryWrapper<ChatGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ChatGroup::getId, groupIds)
                .eq(ChatGroup::getStatus, GroupStatus.NORMAL)
                .select(ChatGroup::getId, ChatGroup::getName, ChatGroup::getAvatar,
                        ChatGroup::getDescription, ChatGroup::getOwnerId, ChatGroup::getMemberCount,
                        ChatGroup::getCreatedAt);

        List<ChatGroup> groups = chatGroupMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyList();
        }

        // 查询用户在各群的角色
        return groups.stream()
                .map(g -> {
                    GroupMember member = getMemberByGroupAndUser(g.getId(), userId);
                    return GroupVO.builder()
                            .id(g.getId())
                            .name(g.getName())
                            .avatar(g.getAvatar())
                            .description(g.getDescription())
                            .ownerId(g.getOwnerId())
                            .memberCount(g.getMemberCount())
                            .myRole(member != null ? member.getRole() : null)
                            .createdAt(g.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupMemberVO> getGroupMembers(Long groupId) {
        // 验证群组存在
        getGroupById(groupId);

        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .orderByDesc(GroupMember::getRole)
                .orderByAsc(GroupMember::getJoinedAt);

        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }

        // TODO: 调用用户服务获取用户详细信息（昵称、头像等）
        return members.stream()
                .map(m -> GroupMemberVO.builder()
                        .userId(m.getUserId())
                        .nickname(m.getNickname())
                        .role(m.getRole())
                        .muted(m.getMuted())
                        .mutedUntil(m.getMutedUntil())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getGroupMemberIds(Long groupId) {
        // 先从缓存获取
        String cacheKey = GROUP_MEMBER_CACHE_KEY_PREFIX + groupId;
        Set<String> cachedIds = redisTemplate.opsForSet().members(cacheKey);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            return cachedIds.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        // 验证群组存在
        getGroupById(groupId);

        // 从数据库查询
        List<Long> memberIds = groupMemberMapper.selectMemberIdsByGroupId(groupId);

        // 写入缓存
        cacheGroupMemberIds(groupId, memberIds);

        return memberIds;
    }

    @Override
    public boolean isGroupMember(Long groupId, Long userId) {
        // 先从缓存检查
        String cacheKey = GROUP_MEMBER_CACHE_KEY_PREFIX + groupId;
        Boolean isMember = redisTemplate.opsForSet().isMember(cacheKey, String.valueOf(userId));
        if (Boolean.TRUE.equals(isMember)) {
            return true;
        }

        // 缓存未命中或不存在，从数据库查询
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        return groupMemberMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setAdmin(Long groupId, Long operatorId, Long memberId, boolean isAdmin) {
        ChatGroup group = getGroupById(groupId);

        // 只有群主可以设置管理员
        if (!group.getOwnerId().equals(operatorId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_OWNER);
        }

        // 不能设置群主为管理员
        if (group.getOwnerId().equals(memberId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NO_PERMISSION);
        }

        GroupMember member = getMemberByGroupAndUser(groupId, memberId);
        if (member == null) {
            throw new BusinessException.DataNotFoundException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        GroupMemberRole newRole = isAdmin ? GroupMemberRole.ADMIN : GroupMemberRole.MEMBER;

        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getId, member.getId())
                .set(GroupMember::getRole, newRole)
                .set(GroupMember::getUpdatedAt, LocalDateTime.now());
        groupMemberMapper.update(null, updateWrapper);

        log.info("Admin status changed: groupId={}, member={}, isAdmin={}", groupId, memberId, isAdmin);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteMember(Long groupId, Long operatorId, Long memberId, int minutes) {
        ChatGroup group = getGroupById(groupId);

        // 检查操作人权限
        GroupMember operatorMember = getMemberByGroupAndUser(groupId, operatorId);
        if (operatorMember == null) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        if (!operatorMember.getRole().isOwner() && !operatorMember.getRole().isAdmin()) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_ADMIN);
        }

        // 不能禁言群主
        if (group.getOwnerId().equals(memberId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.CANNOT_MUTE_ADMIN);
        }

        GroupMember targetMember = getMemberByGroupAndUser(groupId, memberId);
        if (targetMember == null) {
            throw new BusinessException.DataNotFoundException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        // 管理员不能禁言其他管理员
        if (!operatorMember.getRole().isOwner() && targetMember.getRole().isAdmin()) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.CANNOT_MUTE_ADMIN);
        }

        // 设置禁言
        BooleanFlag muted;
        LocalDateTime mutedUntil;

        if (minutes == 0) {
            // 解除禁言
            muted = BooleanFlag.NO;
            mutedUntil = null;
        } else if (minutes < 0) {
            // 永久禁言
            muted = BooleanFlag.YES;
            mutedUntil = null;
        } else {
            // 定时禁言
            muted = BooleanFlag.YES;
            mutedUntil = LocalDateTime.now().plusMinutes(minutes);
        }

        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getId, targetMember.getId())
                .set(GroupMember::getMuted, muted)
                .set(GroupMember::getMutedUntil, mutedUntil)
                .set(GroupMember::getUpdatedAt, LocalDateTime.now());
        groupMemberMapper.update(null, updateWrapper);

        log.info("Member mute status changed: groupId={}, member={}, minutes={}", groupId, memberId, minutes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupInfo(Long groupId, Long operatorId, String name, String avatar, String description) {
        ChatGroup group = getGroupById(groupId);

        // 检查操作人权限（群主或管理员）
        GroupMember operatorMember = getMemberByGroupAndUser(groupId, operatorId);
        if (operatorMember == null) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        if (!operatorMember.getRole().isOwner() && !operatorMember.getRole().isAdmin()) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_ADMIN);
        }

        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatGroup::getId, groupId);

        if (StringUtils.hasText(name)) {
            updateWrapper.set(ChatGroup::getName, name);
        }
        if (avatar != null) {
            updateWrapper.set(ChatGroup::getAvatar, avatar);
        }
        if (description != null) {
            updateWrapper.set(ChatGroup::getDescription, description);
        }
        updateWrapper.set(ChatGroup::getUpdatedAt, LocalDateTime.now());

        chatGroupMapper.update(null, updateWrapper);

        log.info("Group info updated: groupId={}, operator={}", groupId, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refreshGroupCode(Long groupId, Long operatorId) {
        ChatGroup group = getGroupById(groupId);

        // 检查操作人权限（群主或管理员）
        GroupMember operatorMember = getMemberByGroupAndUser(groupId, operatorId);
        if (operatorMember == null) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_MEMBER);
        }

        if (!operatorMember.getRole().isOwner() && !operatorMember.getRole().isAdmin()) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NOT_GROUP_ADMIN);
        }

        String newCode = generateGroupCode();

        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatGroup::getId, groupId)
                .set(ChatGroup::getGroupCode, newCode)
                .set(ChatGroup::getUpdatedAt, LocalDateTime.now());
        chatGroupMapper.update(null, updateWrapper);

        log.info("Group code refreshed: groupId={}, operator={}", groupId, operatorId);
        return newCode;
    }

    // ==================== 私有方法 ====================

    private ChatGroup getGroupById(Long groupId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null || group.getStatus().isDissolved()) {
            throw new BusinessException.DataNotFoundException(ChatExceptionMessage.GROUP_NOT_FOUND);
        }
        return group;
    }

    private GroupMember getMemberByGroupAndUser(Long groupId, Long userId) {
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        return groupMemberMapper.selectOne(queryWrapper);
    }

    private void addMember(Long groupId, Long userId, GroupMemberRole role, Long inviterId) {
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
    }

    private void updateMemberCount(Long groupId) {
        LambdaQueryWrapper<GroupMember> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(GroupMember::getGroupId, groupId);
        long count = groupMemberMapper.selectCount(countWrapper);

        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatGroup::getId, groupId)
                .set(ChatGroup::getMemberCount, (int) count)
                .set(ChatGroup::getUpdatedAt, LocalDateTime.now());
        chatGroupMapper.update(null, updateWrapper);
    }

    private String generateGroupCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, GROUP_CODE_LENGTH).toUpperCase();
    }

    /**
     * 缓存群成员ID列表
     */
    private void cacheGroupMemberIds(Long groupId, List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }
        String cacheKey = GROUP_MEMBER_CACHE_KEY_PREFIX + groupId;
        String[] ids = memberIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
        redisTemplate.opsForSet().add(cacheKey, ids);
        redisTemplate.expire(cacheKey, GROUP_MEMBER_CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 清除群成员缓存
     */
    private void clearGroupMemberCache(Long groupId) {
        String cacheKey = GROUP_MEMBER_CACHE_KEY_PREFIX + groupId;
        redisTemplate.delete(cacheKey);
    }
}
