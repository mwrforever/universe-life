package com.universe.life.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.message.domain.po.FriendRequest;
import com.universe.life.message.domain.po.UserFriend;
import com.universe.life.message.domain.vo.FriendRequestVO;
import com.universe.life.message.domain.vo.FriendVO;
import com.universe.life.message.enums.FriendRequestStatus;
import com.universe.life.message.enums.FriendStatus;
import com.universe.life.message.exception.ChatExceptionMessage;
import com.universe.life.message.mapper.FriendRequestMapper;
import com.universe.life.message.mapper.UserFriendMapper;
import com.universe.life.message.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 好友服务实现类
 *
 * @author Kiro
 * @since 2026/01/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private static final int REQUEST_EXPIRE_DAYS = 7;
    private static final String FRIEND_CACHE_KEY_PREFIX = "chat:friend:list:";
    private static final long FRIEND_CACHE_TTL_HOURS = 24;

    private final UserFriendMapper userFriendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        // 不能添加自己为好友
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.CANNOT_ADD_SELF);
        }

        // 检查是否已经是好友
        if (isFriend(fromUserId, toUserId)) {
            throw new BusinessException.DataAlreadyExistsException(ChatExceptionMessage.ALREADY_FRIEND);
        }

        // 检查是否已有待处理的请求
        LambdaQueryWrapper<FriendRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendRequest::getFromUserId, fromUserId)
                .eq(FriendRequest::getToUserId, toUserId)
                .eq(FriendRequest::getStatus, FriendRequestStatus.PENDING);
        
        if (friendRequestMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException.DataAlreadyExistsException(ChatExceptionMessage.FRIEND_REQUEST_ALREADY_SENT);
        }

        // 创建好友请求
        FriendRequest request = FriendRequest.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .message(message)
                .status(FriendRequestStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusDays(REQUEST_EXPIRE_DAYS))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        friendRequestMapper.insert(request);
        log.info("Friend request sent from {} to {}", fromUserId, toUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest request = getFriendRequestById(requestId);

        // 验证是目标用户
        if (!request.getToUserId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NO_PERMISSION);
        }

        // 验证状态
        if (!request.getStatus().isPending()) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.FRIEND_REQUEST_ALREADY_HANDLED);
        }

        // 检查是否过期
        if (request.getExpiredAt() != null && request.getExpiredAt().isBefore(LocalDateTime.now())) {
            // 更新状态为已过期
            updateRequestStatus(requestId, FriendRequestStatus.EXPIRED);
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.FRIEND_REQUEST_EXPIRED);
        }

        // 更新请求状态
        updateRequestStatus(requestId, FriendRequestStatus.ACCEPTED);

        // 建立双向好友关系
        createFriendRelation(request.getFromUserId(), request.getToUserId());
        createFriendRelation(request.getToUserId(), request.getFromUserId());

        // 清除双方好友缓存
        clearFriendCache(request.getFromUserId());
        clearFriendCache(request.getToUserId());

        log.info("Friend request {} accepted, {} and {} are now friends", requestId, request.getFromUserId(), request.getToUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectFriendRequest(Long requestId, Long userId) {
        FriendRequest request = getFriendRequestById(requestId);

        // 验证是目标用户
        if (!request.getToUserId().equals(userId)) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.NO_PERMISSION);
        }

        // 验证状态
        if (!request.getStatus().isPending()) {
            throw new BusinessException.OperationNotAllowedException(ChatExceptionMessage.FRIEND_REQUEST_ALREADY_HANDLED);
        }

        // 更新请求状态
        updateRequestStatus(requestId, FriendRequestStatus.REJECTED);

        log.info("Friend request {} rejected by user {}", requestId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendId) {
        // 检查是否是好友
        if (!isFriend(userId, friendId)) {
            throw new BusinessException.DataNotFoundException(
                    ExceptionMessage.Formatter.recordNotFound("好友关系"));
        }

        // 删除双向好友关系（软删除）
        deleteFriendRelation(userId, friendId);
        deleteFriendRelation(friendId, userId);

        // 清除双方好友缓存
        clearFriendCache(userId);
        clearFriendCache(friendId);

        log.info("Friend relation deleted between {} and {}", userId, friendId);
    }

    @Override
    public List<FriendVO> getFriendList(Long userId) {
        LambdaQueryWrapper<UserFriend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getStatus, FriendStatus.NORMAL)
                .select(UserFriend::getFriendId, UserFriend::getRemark, UserFriend::getCreatedAt);

        List<UserFriend> friends = userFriendMapper.selectList(queryWrapper);
        if (friends == null || friends.isEmpty()) {
            return Collections.emptyList();
        }

        // TODO: 调用用户服务获取好友详细信息（昵称、头像等）
        return friends.stream()
                .map(f -> FriendVO.builder()
                        .friendId(f.getFriendId())
                        .remark(f.getRemark())
                        .createdAt(f.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getFriendIds(Long userId) {
        // 先从缓存获取
        String cacheKey = FRIEND_CACHE_KEY_PREFIX + userId;
        Set<String> cachedIds = redisTemplate.opsForSet().members(cacheKey);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            return cachedIds.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        // 缓存未命中，从数据库查询
        LambdaQueryWrapper<UserFriend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getStatus, FriendStatus.NORMAL)
                .select(UserFriend::getFriendId);

        List<UserFriend> friends = userFriendMapper.selectList(queryWrapper);
        if (friends == null || friends.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> friendIds = friends.stream()
                .map(UserFriend::getFriendId)
                .collect(Collectors.toList());

        // 写入缓存
        cacheFriendIds(userId, friendIds);

        return friendIds;
    }

    @Override
    public boolean isFriend(Long userId1, Long userId2) {
        // 先从缓存检查
        String cacheKey = FRIEND_CACHE_KEY_PREFIX + userId1;
        Boolean isMember = redisTemplate.opsForSet().isMember(cacheKey, String.valueOf(userId2));
        if (Boolean.TRUE.equals(isMember)) {
            return true;
        }

        // 缓存未命中或不存在，从数据库查询
        LambdaQueryWrapper<UserFriend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriend::getUserId, userId1)
                .eq(UserFriend::getFriendId, userId2)
                .eq(UserFriend::getStatus, FriendStatus.NORMAL);

        return userFriendMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<FriendRequestVO> getPendingFriendRequests(Long userId) {
        LambdaQueryWrapper<FriendRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, FriendRequestStatus.PENDING)
                .orderByDesc(FriendRequest::getCreatedAt);

        List<FriendRequest> requests = friendRequestMapper.selectList(queryWrapper);
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        // TODO: 调用用户服务获取申请人详细信息
        return requests.stream()
                .map(r -> FriendRequestVO.builder()
                        .id(r.getId())
                        .fromUserId(r.getFromUserId())
                        .message(r.getMessage())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .expiredAt(r.getExpiredAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendRequestVO> getSentFriendRequests(Long userId) {
        LambdaQueryWrapper<FriendRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendRequest::getFromUserId, userId)
                .orderByDesc(FriendRequest::getCreatedAt);

        List<FriendRequest> requests = friendRequestMapper.selectList(queryWrapper);
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        return requests.stream()
                .map(r -> FriendRequestVO.builder()
                        .id(r.getId())
                        .fromUserId(r.getFromUserId())
                        .message(r.getMessage())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .expiredAt(r.getExpiredAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void updateFriendRemark(Long userId, Long friendId, String remark) {
        if (!isFriend(userId, friendId)) {
            throw new BusinessException.DataNotFoundException(
                    ExceptionMessage.Formatter.recordNotFound("好友关系"));
        }

        LambdaUpdateWrapper<UserFriend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getFriendId, friendId)
                .set(UserFriend::getRemark, remark)
                .set(UserFriend::getUpdatedAt, LocalDateTime.now());

        userFriendMapper.update(null, updateWrapper);
        log.info("Updated friend remark for user {} friend {}", userId, friendId);
    }

    private FriendRequest getFriendRequestById(Long requestId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException.DataNotFoundException(ChatExceptionMessage.FRIEND_REQUEST_NOT_FOUND);
        }
        return request;
    }

    private void updateRequestStatus(Long requestId, FriendRequestStatus status) {
        LambdaUpdateWrapper<FriendRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FriendRequest::getId, requestId)
                .set(FriendRequest::getStatus, status)
                .set(FriendRequest::getHandledAt, LocalDateTime.now())
                .set(FriendRequest::getUpdatedAt, LocalDateTime.now());

        friendRequestMapper.update(null, updateWrapper);
    }

    private void createFriendRelation(Long userId, Long friendId) {
        // 检查是否已存在（可能是之前删除的）
        LambdaQueryWrapper<UserFriend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getFriendId, friendId);

        UserFriend existing = userFriendMapper.selectOne(queryWrapper);
        if (existing != null) {
            // 恢复已删除的好友关系
            LambdaUpdateWrapper<UserFriend> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserFriend::getId, existing.getId())
                    .set(UserFriend::getStatus, FriendStatus.NORMAL)
                    .set(UserFriend::getUpdatedAt, LocalDateTime.now());
            userFriendMapper.update(null, updateWrapper);
        } else {
            // 创建新的好友关系
            UserFriend friend = UserFriend.builder()
                    .userId(userId)
                    .friendId(friendId)
                    .status(FriendStatus.NORMAL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userFriendMapper.insert(friend);
        }
    }

    private void deleteFriendRelation(Long userId, Long friendId) {
        LambdaUpdateWrapper<UserFriend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getFriendId, friendId)
                .set(UserFriend::getStatus, FriendStatus.DELETED)
                .set(UserFriend::getUpdatedAt, LocalDateTime.now());

        userFriendMapper.update(null, updateWrapper);
    }

    /**
     * 缓存好友ID列表
     */
    private void cacheFriendIds(Long userId, List<Long> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return;
        }
        String cacheKey = FRIEND_CACHE_KEY_PREFIX + userId;
        String[] ids = friendIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
        redisTemplate.opsForSet().add(cacheKey, ids);
        redisTemplate.expire(cacheKey, FRIEND_CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 清除好友缓存
     */
    private void clearFriendCache(Long userId) {
        String cacheKey = FRIEND_CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }
}
