package com.universe.life.message.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.message.domain.dto.request.CreateGroupRequest;
import com.universe.life.message.domain.vo.GroupMemberVO;
import com.universe.life.message.domain.vo.GroupVO;
import com.universe.life.message.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组管理 Controller
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Tag(name = "群组管理", description = "群组创建、邀请、管理等接口")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "创建群组")
    @PostMapping
    public Result<Long> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Long userId = SecurityUtil.getUserId();
        Long groupId = groupService.createGroup(request, userId);
        return Result.success(groupId);
    }

    @Operation(summary = "获取群组详情")
    @GetMapping("/{groupId}")
    public Result<GroupVO> getGroupDetail(@PathVariable Long groupId) {
        Long userId = SecurityUtil.getUserId();
        GroupVO group = groupService.getGroupDetail(groupId, userId);
        return Result.success(group);
    }

    @Operation(summary = "获取我的群组列表")
    @GetMapping("/my")
    public Result<List<GroupVO>> getMyGroups() {
        Long userId = SecurityUtil.getUserId();
        List<GroupVO> groups = groupService.getMyGroups(userId);
        return Result.success(groups);
    }

    @Operation(summary = "获取群组成员列表")
    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberVO>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberVO> members = groupService.getGroupMembers(groupId);
        return Result.success(members);
    }

    @Operation(summary = "邀请用户入群")
    @PostMapping("/{groupId}/invite")
    public Result<Void> inviteToGroup(
            @PathVariable Long groupId,
            @RequestBody List<Long> userIds) {
        Long userId = SecurityUtil.getUserId();
        groupService.inviteToGroup(groupId, userId, userIds);
        return Result.success();
    }

    @Operation(summary = "通过群聊码加入群组")
    @PostMapping("/join")
    public Result<Void> joinByCode(@RequestParam String groupCode) {
        Long userId = SecurityUtil.getUserId();
        groupService.joinByCode(groupCode, userId);
        return Result.success();
    }

    @Operation(summary = "移除群成员")
    @DeleteMapping("/{groupId}/members/{memberId}")
    public Result<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        Long userId = SecurityUtil.getUserId();
        groupService.removeMember(groupId, userId, memberId);
        return Result.success();
    }

    @Operation(summary = "退出群组")
    @PostMapping("/{groupId}/leave")
    public Result<Void> leaveGroup(@PathVariable Long groupId) {
        Long userId = SecurityUtil.getUserId();
        groupService.leaveGroup(groupId, userId);
        return Result.success();
    }

    @Operation(summary = "转让群主")
    @PostMapping("/{groupId}/transfer")
    public Result<Void> transferOwner(
            @PathVariable Long groupId,
            @RequestParam Long newOwnerId) {
        Long userId = SecurityUtil.getUserId();
        groupService.transferOwner(groupId, userId, newOwnerId);
        return Result.success();
    }

    @Operation(summary = "解散群组")
    @DeleteMapping("/{groupId}")
    public Result<Void> dissolveGroup(@PathVariable Long groupId) {
        Long userId = SecurityUtil.getUserId();
        groupService.dissolveGroup(groupId, userId);
        return Result.success();
    }

    @Operation(summary = "设置/取消管理员")
    @PostMapping("/{groupId}/admin")
    public Result<Void> setAdmin(
            @PathVariable Long groupId,
            @RequestParam Long memberId,
            @RequestParam Boolean isAdmin) {
        Long userId = SecurityUtil.getUserId();
        groupService.setAdmin(groupId, userId, memberId, isAdmin);
        return Result.success();
    }

    @Operation(summary = "禁言/解禁成员")
    @PostMapping("/{groupId}/mute")
    public Result<Void> muteMember(
            @PathVariable Long groupId,
            @RequestParam Long memberId,
            @RequestParam Integer minutes) {
        Long userId = SecurityUtil.getUserId();
        groupService.muteMember(groupId, userId, memberId, minutes);
        return Result.success();
    }

    @Operation(summary = "更新群组信息")
    @PutMapping("/{groupId}")
    public Result<Void> updateGroupInfo(
            @PathVariable Long groupId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) String description) {
        Long userId = SecurityUtil.getUserId();
        groupService.updateGroupInfo(groupId, userId, name, avatar, description);
        return Result.success();
    }

    @Operation(summary = "刷新群聊码")
    @PostMapping("/{groupId}/refresh-code")
    public Result<String> refreshGroupCode(@PathVariable Long groupId) {
        Long userId = SecurityUtil.getUserId();
        String newCode = groupService.refreshGroupCode(groupId, userId);
        return Result.success(newCode);
    }
}
