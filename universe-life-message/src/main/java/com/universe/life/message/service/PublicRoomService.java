package com.universe.life.message.service;

import com.universe.life.message.domain.dto.request.CreateRoomRequest;
import com.universe.life.message.domain.vo.PublicRoomVO;

import java.util.List;

/**
 * 公共聊天室服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface PublicRoomService {

    /**
     * 创建公共聊天室
     *
     * @param request   创建请求
     * @param creatorId 创建者ID
     * @return 聊天室ID
     */
    Long createRoom(CreateRoomRequest request, Long creatorId);

    /**
     * 获取聊天室详情
     *
     * @param roomId 聊天室ID
     * @return 聊天室信息
     */
    PublicRoomVO getRoomDetail(Long roomId);

    /**
     * 获取聊天室列表
     *
     * @param category 聊天室分类（可选）
     * @return 聊天室列表
     */
    List<PublicRoomVO> getRoomList(String category);

    /**
     * 加入聊天室
     *
     * @param roomId   聊天室ID
     * @param userId   用户ID
     */
    void joinRoom(Long roomId, Long userId);

    /**
     * 离开聊天室
     *
     * @param roomId 聊天室ID
     * @param userId 用户ID
     */
    void leaveRoom(Long roomId, Long userId);

    /**
     * 关闭聊天室（管理员操作）
     *
     * @param roomId 聊天室ID
     */
    void closeRoom(Long roomId);

    /**
     * 更新聊天室信息（管理员操作）
     *
     * @param roomId      聊天室ID
     * @param name        名称
     * @param avatar      头像
     * @param description 描述
     */
    void updateRoomInfo(Long roomId, String name, String avatar, String description);

    /**
     * 增加在线人数
     *
     * @param roomId 聊天室ID
     */
    void incrementOnlineCount(Long roomId);

    /**
     * 减少在线人数
     *
     * @param roomId 聊天室ID
     */
    void decrementOnlineCount(Long roomId);

    /**
     * 检查聊天室是否存在且正常
     *
     * @param roomId 聊天室ID
     * @return 是否存在且正常
     */
    boolean isRoomActive(Long roomId);
}
