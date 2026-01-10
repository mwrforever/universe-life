package com.universe.life.message.service;

import com.universe.life.message.domain.dto.request.UpdateChatSettingRequest;
import com.universe.life.message.domain.vo.UserChatSettingVO;

/**
 * 用户聊天设置服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface UserChatSettingService {

    /**
     * 获取用户聊天设置
     *
     * @param userId 用户ID
     * @return 聊天设置
     */
    UserChatSettingVO getUserChatSetting(Long userId);

    /**
     * 更新用户聊天设置
     *
     * @param userId  用户ID
     * @param request 更新请求
     */
    void updateUserChatSetting(Long userId, UpdateChatSettingRequest request);

    /**
     * 检查用户是否允许公共单人会话
     *
     * @param userId 用户ID
     * @return 是否允许
     */
    boolean isAllowPublicMessage(Long userId);

    /**
     * 检查用户是否允许陌生人消息
     *
     * @param userId 用户ID
     * @return 是否允许
     */
    boolean isAllowStrangerMessage(Long userId);
}
