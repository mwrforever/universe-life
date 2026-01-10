package com.universe.life.message.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.message.domain.dto.request.CreateRoomRequest;
import com.universe.life.message.domain.vo.PublicRoomVO;
import com.universe.life.message.service.PublicRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共聊天室 Controller
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Tag(name = "公共聊天室", description = "公共聊天室创建、加入、管理等接口")
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class PublicRoomController {

    private final PublicRoomService publicRoomService;

    @Operation(summary = "创建公共聊天室")
    @PostMapping
    public Result<Long> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Long userId = SecurityUtil.getUserId();
        Long roomId = publicRoomService.createRoom(request, userId);
        return Result.success(roomId);
    }

    @Operation(summary = "获取聊天室详情")
    @GetMapping("/{roomId}")
    public Result<PublicRoomVO> getRoomDetail(@PathVariable Long roomId) {
        PublicRoomVO room = publicRoomService.getRoomDetail(roomId);
        return Result.success(room);
    }

    @Operation(summary = "获取聊天室列表")
    @GetMapping
    public Result<List<PublicRoomVO>> getRoomList(
            @RequestParam(required = false) String category) {
        List<PublicRoomVO> rooms = publicRoomService.getRoomList(category);
        return Result.success(rooms);
    }

    @Operation(summary = "加入聊天室")
    @PostMapping("/{roomId}/join")
    public Result<Void> joinRoom(@PathVariable Long roomId) {
        Long userId = SecurityUtil.getUserId();
        publicRoomService.joinRoom(roomId, userId);
        return Result.success();
    }

    @Operation(summary = "离开聊天室")
    @PostMapping("/{roomId}/leave")
    public Result<Void> leaveRoom(@PathVariable Long roomId) {
        Long userId = SecurityUtil.getUserId();
        publicRoomService.leaveRoom(roomId, userId);
        return Result.success();
    }

    @Operation(summary = "关闭聊天室")
    @DeleteMapping("/{roomId}")
    public Result<Void> closeRoom(@PathVariable Long roomId) {
        publicRoomService.closeRoom(roomId);
        return Result.success();
    }

    @Operation(summary = "更新聊天室信息")
    @PutMapping("/{roomId}")
    public Result<Void> updateRoomInfo(
            @PathVariable Long roomId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) String description) {
        publicRoomService.updateRoomInfo(roomId, name, avatar, description);
        return Result.success();
    }
}
