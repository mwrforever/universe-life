package com.universe.life.message.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.message.domain.dto.request.HistoryMessageRequest;
import com.universe.life.message.domain.dto.request.SyncMessageRequest;
import com.universe.life.message.domain.vo.ChatMessageVO;
import com.universe.life.message.service.MessagePersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息 REST API 控制器
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Tag(name = "消息管理", description = "消息历史查询、消息同步等接口")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessagePersistenceService messagePersistenceService;

    @Operation(summary = "查询历史消息")
    @PostMapping("/history")
    public Result<Page<ChatMessageVO>> getHistoryMessages(
            @Valid @RequestBody HistoryMessageRequest request) {
        Long userId = SecurityUtil.getUserId();
        Page<ChatMessageVO> page = messagePersistenceService.getHistoryMessages(
                userId,
                request.getTargetId(),
                request.getMessageType(),
                request.getPageNum(),
                request.getPageSize()
        );
        return Result.success(page);
    }

    @Operation(summary = "同步消息（消息漫游）")
    @PostMapping("/sync")
    public Result<List<ChatMessageVO>> syncMessages(
            @Valid @RequestBody SyncMessageRequest request) {
        Long userId = SecurityUtil.getUserId();
        List<ChatMessageVO> messages = messagePersistenceService.syncMessages(
                userId,
                request.getLastSequence(),
                request.getLimit()
        );
        return Result.success(messages);
    }

    @Operation(summary = "根据消息ID获取消息详情")
    @GetMapping("/{messageId}")
    public Result<ChatMessageVO> getMessageById(
            @Parameter(description = "消息ID") @PathVariable String messageId) {
        ChatMessageVO message = messagePersistenceService.getMessageById(messageId);
        return Result.success(message);
    }
}
