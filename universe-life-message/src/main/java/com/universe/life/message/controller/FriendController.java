package com.universe.life.message.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.message.domain.dto.request.SendFriendRequestRequest;
import com.universe.life.message.domain.vo.FriendRequestVO;
import com.universe.life.message.domain.vo.FriendVO;
import com.universe.life.message.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友管理控制器
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Tag(name = "好友管理", description = "好友关系管理接口")
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    @Operation(summary = "获取好友列表", description = "获取当前用户的好友列表")
    public Result<List<FriendVO>> getFriendList() {
        Long userId = SecurityUtil.getUserId();
        List<FriendVO> friends = friendService.getFriendList(userId);
        return Result.success(friends);
    }

    @GetMapping("/ids")
    @Operation(summary = "获取好友ID列表", description = "获取当前用户的好友ID列表")
    public Result<List<Long>> getFriendIds() {
        Long userId = SecurityUtil.getUserId();
        List<Long> friendIds = friendService.getFriendIds(userId);
        return Result.success(friendIds);
    }

    @GetMapping("/check/{friendId}")
    @Operation(summary = "检查是否为好友", description = "检查指定用户是否为当前用户的好友")
    public Result<Boolean> checkIsFriend(
            @Parameter(description = "好友ID") @PathVariable Long friendId) {
        Long userId = SecurityUtil.getUserId();
        boolean isFriend = friendService.isFriend(userId, friendId);
        return Result.success(isFriend);
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "删除好友", description = "删除指定好友")
    public Result<Void> deleteFriend(
            @Parameter(description = "好友ID") @PathVariable Long friendId) {
        Long userId = SecurityUtil.getUserId();
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }

    @PutMapping("/{friendId}/remark")
    @Operation(summary = "修改好友备注", description = "修改好友的备注名")
    public Result<Void> updateFriendRemark(
            @Parameter(description = "好友ID") @PathVariable Long friendId,
            @Parameter(description = "备注名") @RequestParam String remark) {
        Long userId = SecurityUtil.getUserId();
        friendService.updateFriendRemark(userId, friendId, remark);
        return Result.success();
    }

    @PostMapping("/requests")
    @Operation(summary = "发送好友请求", description = "向指定用户发送好友请求")
    public Result<Void> sendFriendRequest(@Valid @RequestBody SendFriendRequestRequest request) {
        Long userId = SecurityUtil.getUserId();
        friendService.sendFriendRequest(userId, request.getToUserId(), request.getMessage());
        return Result.success();
    }

    @GetMapping("/requests/pending")
    @Operation(summary = "获取待处理的好友请求", description = "获取收到的待处理好友请求列表")
    public Result<List<FriendRequestVO>> getPendingFriendRequests() {
        Long userId = SecurityUtil.getUserId();
        List<FriendRequestVO> requests = friendService.getPendingFriendRequests(userId);
        return Result.success(requests);
    }

    @GetMapping("/requests/sent")
    @Operation(summary = "获取已发送的好友请求", description = "获取自己发送的好友请求列表")
    public Result<List<FriendRequestVO>> getSentFriendRequests() {
        Long userId = SecurityUtil.getUserId();
        List<FriendRequestVO> requests = friendService.getSentFriendRequests(userId);
        return Result.success(requests);
    }

    @PostMapping("/requests/{requestId}/accept")
    @Operation(summary = "接受好友请求", description = "接受指定的好友请求")
    public Result<Void> acceptFriendRequest(
            @Parameter(description = "请求ID") @PathVariable Long requestId) {
        Long userId = SecurityUtil.getUserId();
        friendService.acceptFriendRequest(requestId, userId);
        return Result.success();
    }

    @PostMapping("/requests/{requestId}/reject")
    @Operation(summary = "拒绝好友请求", description = "拒绝指定的好友请求")
    public Result<Void> rejectFriendRequest(
            @Parameter(description = "请求ID") @PathVariable Long requestId) {
        Long userId = SecurityUtil.getUserId();
        friendService.rejectFriendRequest(requestId, userId);
        return Result.success();
    }
}
