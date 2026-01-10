package com.universe.life.message.controller;

import com.universe.life.auth.common.domain.Result;
import com.universe.life.auth.resource.util.SecurityUtil;
import com.universe.life.message.domain.dto.request.UpdateChatSettingRequest;
import com.universe.life.message.domain.vo.UserChatSettingVO;
import com.universe.life.message.service.UserChatSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户聊天设置 Controller
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Tag(name = "用户聊天设置", description = "用户聊天相关设置接口")
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class UserChatSettingController {

    private final UserChatSettingService userChatSettingService;

    @Operation(summary = "获取聊天设置")
    @GetMapping
    public Result<UserChatSettingVO> getChatSetting() {
        Long userId = SecurityUtil.getUserId();
        UserChatSettingVO setting = userChatSettingService.getUserChatSetting(userId);
        return Result.success(setting);
    }

    @Operation(summary = "更新聊天设置")
    @PutMapping
    public Result<Void> updateChatSetting(@RequestBody UpdateChatSettingRequest request) {
        Long userId = SecurityUtil.getUserId();
        userChatSettingService.updateUserChatSetting(userId, request);
        return Result.success();
    }
}
