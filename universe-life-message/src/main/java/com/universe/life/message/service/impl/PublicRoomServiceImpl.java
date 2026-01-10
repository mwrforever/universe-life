package com.universe.life.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.message.domain.dto.request.CreateRoomRequest;
import com.universe.life.message.domain.po.PublicRoom;
import com.universe.life.message.domain.vo.PublicRoomVO;
import com.universe.life.message.enums.RoomStatus;
import com.universe.life.message.exception.ChatExceptionMessage;
import com.universe.life.message.mapper.PublicRoomMapper;
import com.universe.life.message.service.PublicRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公共聊天室服务实现类
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublicRoomServiceImpl implements PublicRoomService {

    private static final int DEFAULT_MAX_ONLINE = 1000;

    private final PublicRoomMapper publicRoomMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoom(CreateRoomRequest request, Long creatorId) {
        PublicRoom room = PublicRoom.builder()
                .name(request.getName())
                .avatar(request.getAvatar())
                .description(request.getDescription())
                .category(request.getCategory())
                .maxOnline(request.getMaxOnline() != null ? request.getMaxOnline() : DEFAULT_MAX_ONLINE)
                .currentOnline(0)
                .totalMembers(0)
                .status(RoomStatus.NORMAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        publicRoomMapper.insert(room);
        log.info("Public room created: id={}, name={}, creator={}", room.getId(), request.getName(), creatorId);
        return room.getId();
    }

    @Override
    public PublicRoomVO getRoomDetail(Long roomId) {
        PublicRoom room = getRoomById(roomId);
        return convertToVO(room);
    }

    @Override
    public List<PublicRoomVO> getRoomList(String category) {
        LambdaQueryWrapper<PublicRoom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PublicRoom::getStatus, RoomStatus.NORMAL);

        if (StringUtils.hasText(category)) {
            queryWrapper.eq(PublicRoom::getCategory, category);
        }

        queryWrapper.orderByDesc(PublicRoom::getCurrentOnline)
                .orderByDesc(PublicRoom::getCreatedAt);

        List<PublicRoom> rooms = publicRoomMapper.selectList(queryWrapper);
        if (rooms == null || rooms.isEmpty()) {
            return Collections.emptyList();
        }

        return rooms.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinRoom(Long roomId, Long userId) {
        PublicRoom room = getRoomById(roomId);

        // 检查是否已满
        if (room.getCurrentOnline() >= room.getMaxOnline()) {
            throw new BusinessException.ParamException(ChatExceptionMessage.ROOM_FULL);
        }

        // 增加在线人数和累计人数
        LambdaUpdateWrapper<PublicRoom> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PublicRoom::getId, roomId)
                .setSql("current_online = current_online + 1")
                .setSql("total_members = total_members + 1")
                .set(PublicRoom::getUpdatedAt, LocalDateTime.now());
        publicRoomMapper.update(null, updateWrapper);

        log.info("User joined room: roomId={}, userId={}", roomId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveRoom(Long roomId, Long userId) {
        // 减少在线人数
        decrementOnlineCount(roomId);
        log.info("User left room: roomId={}, userId={}", roomId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeRoom(Long roomId) {
        getRoomById(roomId);

        LambdaUpdateWrapper<PublicRoom> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PublicRoom::getId, roomId)
                .set(PublicRoom::getStatus, RoomStatus.CLOSED)
                .set(PublicRoom::getUpdatedAt, LocalDateTime.now());
        publicRoomMapper.update(null, updateWrapper);

        log.info("Public room closed: roomId={}", roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoomInfo(Long roomId, String name, String avatar, String description) {
        getRoomById(roomId);

        LambdaUpdateWrapper<PublicRoom> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PublicRoom::getId, roomId);

        if (StringUtils.hasText(name)) {
            updateWrapper.set(PublicRoom::getName, name);
        }
        if (avatar != null) {
            updateWrapper.set(PublicRoom::getAvatar, avatar);
        }
        if (description != null) {
            updateWrapper.set(PublicRoom::getDescription, description);
        }
        updateWrapper.set(PublicRoom::getUpdatedAt, LocalDateTime.now());

        publicRoomMapper.update(null, updateWrapper);

        log.info("Public room info updated: roomId={}", roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementOnlineCount(Long roomId) {
        LambdaUpdateWrapper<PublicRoom> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PublicRoom::getId, roomId)
                .setSql("current_online = current_online + 1")
                .set(PublicRoom::getUpdatedAt, LocalDateTime.now());
        publicRoomMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementOnlineCount(Long roomId) {
        LambdaUpdateWrapper<PublicRoom> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PublicRoom::getId, roomId)
                .gt(PublicRoom::getCurrentOnline, 0)
                .setSql("current_online = current_online - 1")
                .set(PublicRoom::getUpdatedAt, LocalDateTime.now());
        publicRoomMapper.update(null, updateWrapper);
    }

    @Override
    public boolean isRoomActive(Long roomId) {
        LambdaQueryWrapper<PublicRoom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PublicRoom::getId, roomId)
                .eq(PublicRoom::getStatus, RoomStatus.NORMAL);
        return publicRoomMapper.selectCount(queryWrapper) > 0;
    }

    // ==================== 私有方法 ====================

    private PublicRoom getRoomById(Long roomId) {
        PublicRoom room = publicRoomMapper.selectById(roomId);
        if (room == null || room.getStatus().isClosed()) {
            throw new BusinessException.DataNotFoundException(ChatExceptionMessage.ROOM_NOT_FOUND);
        }
        return room;
    }

    private PublicRoomVO convertToVO(PublicRoom room) {
        return PublicRoomVO.builder()
                .id(room.getId())
                .name(room.getName())
                .avatar(room.getAvatar())
                .description(room.getDescription())
                .category(room.getCategory())
                .maxOnline(room.getMaxOnline())
                .currentOnline(room.getCurrentOnline())
                .totalMembers(room.getTotalMembers())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
