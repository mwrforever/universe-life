package com.universe.life.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.universe.life.message.domain.dto.request.UpdateChatSettingRequest;
import com.universe.life.message.domain.po.UserChatSetting;
import com.universe.life.message.domain.vo.UserChatSettingVO;
import com.universe.life.message.enums.BooleanFlag;
import com.universe.life.message.mapper.UserChatSettingMapper;
import com.universe.life.message.service.UserChatSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户聊天设置服务实现类
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserChatSettingServiceImpl implements UserChatSettingService {

    private final UserChatSettingMapper userChatSettingMapper;

    @Override
    public UserChatSettingVO getUserChatSetting(Long userId) {
        UserChatSetting setting = getOrCreateSetting(userId);
        return UserChatSettingVO.builder()
                .allowPublicMessage(setting.getAllowPublicMessage())
                .allowStrangerMessage(setting.getAllowStrangerMessage())
                .messageNotification(setting.getMessageNotification())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserChatSetting(Long userId, UpdateChatSettingRequest request) {
        UserChatSetting setting = getOrCreateSetting(userId);

        LambdaUpdateWrapper<UserChatSetting> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserChatSetting::getId, setting.getId());

        if (request.getAllowPublicMessage() != null) {
            updateWrapper.set(UserChatSetting::getAllowPublicMessage,
                    request.getAllowPublicMessage() ? BooleanFlag.YES : BooleanFlag.NO);
        }
        if (request.getAllowStrangerMessage() != null) {
            updateWrapper.set(UserChatSetting::getAllowStrangerMessage,
                    request.getAllowStrangerMessage() ? BooleanFlag.YES : BooleanFlag.NO);
        }
        if (request.getMessageNotification() != null) {
            updateWrapper.set(UserChatSetting::getMessageNotification,
                    request.getMessageNotification() ? BooleanFlag.YES : BooleanFlag.NO);
        }
        updateWrapper.set(UserChatSetting::getUpdatedAt, LocalDateTime.now());

        userChatSettingMapper.update(null, updateWrapper);
        log.info("User chat setting updated: userId={}", userId);
    }

    @Override
    public boolean isAllowPublicMessage(Long userId) {
        UserChatSetting setting = getSettingByUserId(userId);
        return setting != null && setting.getAllowPublicMessage().isYes();
    }

    @Override
    public boolean isAllowStrangerMessage(Long userId) {
        UserChatSetting setting = getSettingByUserId(userId);
        return setting != null && setting.getAllowStrangerMessage().isYes();
    }

    // ==================== 私有方法 ====================

    private UserChatSetting getSettingByUserId(Long userId) {
        LambdaQueryWrapper<UserChatSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserChatSetting::getUserId, userId);
        return userChatSettingMapper.selectOne(queryWrapper);
    }

    private UserChatSetting getOrCreateSetting(Long userId) {
        UserChatSetting setting = getSettingByUserId(userId);
        if (setting == null) {
            setting = UserChatSetting.builder()
                    .userId(userId)
                    .allowPublicMessage(BooleanFlag.NO)
                    .allowStrangerMessage(BooleanFlag.NO)
                    .messageNotification(BooleanFlag.YES)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userChatSettingMapper.insert(setting);
            log.info("User chat setting created with defaults: userId={}", userId);
        }
        return setting;
    }
}
