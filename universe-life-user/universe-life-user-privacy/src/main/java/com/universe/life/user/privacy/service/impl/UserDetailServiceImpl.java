package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.user.privacy.constants.RedisConstants;
import com.universe.life.user.privacy.domain.dto.request.UserDetailUpdateRequest;
import com.universe.life.user.privacy.domain.po.UserDetail;
import com.universe.life.user.privacy.domain.vo.UserDetailVO;
import com.universe.life.user.privacy.mapper.UserDetailMapper;
import com.universe.life.user.privacy.mapstruct.UserDetailMapstruct;
import com.universe.life.user.privacy.service.IUserDetailService;
import com.universe.life.common.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 用户详情表 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl extends ServiceImpl<UserDetailMapper, UserDetail> implements IUserDetailService {

    private final UserDetailMapstruct userDetailMapstruct;
    private final CacheUtil cacheUtil;

    @Override
    public UserDetailVO getUserDetailByUserId(Long userId) {
        // 1. 尝试从缓存获取
        String cacheKey = RedisConstants.USER_DETAIL_KEY + userId;
        UserDetailVO cached = cacheUtil.get(cacheKey, UserDetailVO.class);
        if (cached != null) {
            return cached;
        }
        
        // 2. 缓存未命中，查询数据库
        UserDetail userDetail = getById(userId);
        UserDetailVO vo;
        if (ObjectUtil.isNull(userDetail)) {
            // 如果用户详情不存在，返回空的VO
            vo = new UserDetailVO();
            vo.setId(userId);
        } else {
            vo = userDetailMapstruct.toVO(userDetail);
        }
        
        // 3. 写入缓存
        cacheUtil.set(cacheKey, vo, RedisConstants.getUserDetailExpire());
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetailVO updateUserDetail(Long userId, UserDetailUpdateRequest request) {
        log.info("更新用户详情，用户ID：{}", userId);

        UserDetail existingDetail = getById(userId);

        UserDetail userDetail = userDetailMapstruct.toPO(request);
        userDetail.setId(userId);

        // 处理生日字段
        if (request.getBirthday() != null && !request.getBirthday().isEmpty()) {
            userDetail.setBirthday(LocalDate.parse(request.getBirthday(), DateTimeFormatter.ISO_LOCAL_DATE));
        }

        boolean success;
        if (ObjectUtil.isNull(existingDetail)) {
            // 不存在则新增
            success = save(userDetail);
        } else {
            // 存在则更新
            success = updateById(userDetail);
        }
        if (!success) {
            throw new BusinessException.OperationFailedException(ExceptionMessage.Formatter.operationFailed("用户详情更新"));
        }
        
        // 删除缓存
        String cacheKey = RedisConstants.USER_DETAIL_KEY + userId;
        cacheUtil.delete(cacheKey);

        log.info("更新用户详情成功，用户ID：{}", userId);
        return getUserDetailByUserId(userId);
    }
}
