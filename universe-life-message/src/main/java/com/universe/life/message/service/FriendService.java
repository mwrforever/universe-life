package com.universe.life.message.service;

import com.universe.life.message.domain.vo.FriendRequestVO;
import com.universe.life.message.domain.vo.FriendVO;

import java.util.List;

/**
 * 好友服务接口
 *
 * @author Kiro
 * @since 2026/01/09
 */
public interface FriendService {

    /**
     * 发送好友请求
     *
     * @param fromUserId 申请人ID
     * @param toUserId   目标用户ID
     * @param message    申请消息
     */
    void sendFriendRequest(Long fromUserId, Long toUserId, String message);

    /**
     * 接受好友请求
     *
     * @param requestId 请求ID
     * @param userId    当前用户ID（目标用户）
     */
    void acceptFriendRequest(Long requestId, Long userId);

    /**
     * 拒绝好友请求
     *
     * @param requestId 请求ID
     * @param userId    当前用户ID（目标用户）
     */
    void rejectFriendRequest(Long requestId, Long userId);

    /**
     * 删除好友
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     */
    void deleteFriend(Long userId, Long friendId);

    /**
     * 获取好友列表
     *
     * @param userId 用户ID
     * @return 好友列表
     */
    List<FriendVO> getFriendList(Long userId);

    /**
     * 获取好友ID列表
     *
     * @param userId 用户ID
     * @return 好友ID列表
     */
    List<Long> getFriendIds(Long userId);

    /**
     * 检查是否为好友
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 是否为好友
     */
    boolean isFriend(Long userId1, Long userId2);

    /**
     * 获取待处理的好友请求列表
     *
     * @param userId 用户ID
     * @return 好友请求列表
     */
    List<FriendRequestVO> getPendingFriendRequests(Long userId);

    /**
     * 获取已发送的好友请求列表
     *
     * @param userId 用户ID
     * @return 好友请求列表
     */
    List<FriendRequestVO> getSentFriendRequests(Long userId);

    /**
     * 修改好友备注
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @param remark   备注名
     */
    void updateFriendRemark(Long userId, Long friendId, String remark);
}
