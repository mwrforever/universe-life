package com.universe.life.message.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.message.domain.vo.ConversationVO;
import com.universe.life.message.enums.ConversationType;
import com.universe.life.message.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话管理 Controller
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Tag(name = "会话管理", description = "会话列表、未读数、置顶、免打扰等接口")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "获取会话列表")
    @GetMapping
    public Result<List<ConversationVO>> getConversationList() {
        Long userId = SecurityUtil.getUserId();
        List<ConversationVO> conversations = conversationService.getConversationList(userId);
        return Result.success(conversations);
    }

    @Operation(summary = "获取总未读消息数")
    @GetMapping("/unread-count")
    public Result<Integer> getTotalUnreadCount() {
        Long userId = SecurityUtil.getUserId();
        int count = conversationService.getTotalUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "清除会话未读数")
    @PostMapping("/{targetId}/clear-unread")
    public Result<Void> clearUnreadCount(
            @PathVariable Long targetId,
            @RequestParam Integer conversationType) {
        Long userId = SecurityUtil.getUserId();
        ConversationType type = ConversationType.of(conversationType);
        conversationService.clearUnreadCount(userId, targetId, type);
        return Result.success();
    }

    @Operation(summary = "设置会话置顶")
    @PostMapping("/{targetId}/top")
    public Result<Void> setConversationTop(
            @PathVariable Long targetId,
            @RequestParam Integer conversationType,
            @RequestParam Boolean isTop) {
        Long userId = SecurityUtil.getUserId();
        ConversationType type = ConversationType.of(conversationType);
        conversationService.setConversationTop(userId, targetId, type, isTop);
        return Result.success();
    }

    @Operation(summary = "设置会话免打扰")
    @PostMapping("/{targetId}/mute")
    public Result<Void> setConversationMuted(
            @PathVariable Long targetId,
            @RequestParam Integer conversationType,
            @RequestParam Boolean isMuted) {
        Long userId = SecurityUtil.getUserId();
        ConversationType type = ConversationType.of(conversationType);
        conversationService.setConversationMuted(userId, targetId, type, isMuted);
        return Result.success();
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{targetId}")
    public Result<Void> deleteConversation(
            @PathVariable Long targetId,
            @RequestParam Integer conversationType) {
        Long userId = SecurityUtil.getUserId();
        ConversationType type = ConversationType.of(conversationType);
        conversationService.deleteConversation(userId, targetId, type);
        return Result.success();
    }
}
