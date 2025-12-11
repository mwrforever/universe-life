package com.universe.life.user.privacy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.user.privacy.domain.dto.request.UserDetailUpdateRequest;
import com.universe.life.user.privacy.domain.po.UserDetail;
import com.universe.life.user.privacy.domain.vo.UserDetailVO;
import com.universe.life.user.privacy.mapper.UserDetailMapper;
import com.universe.life.user.privacy.mapstruct.UserDetailMapstruct;
import com.universe.life.user.privacy.service.IUserDetailService;
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

    @Override
    public UserDetailVO getUserDetailByUserId(Long userId) {
        UserDetail userDetail = getById(userId);
        if (ObjectUtil.isNull(userDetail)) {
            // 如果用户详情不存在，返回空的VO
            UserDetailVO vo = new UserDetailVO();
            vo.setId(userId);
            return vo;
        }
        return userDetailMapstruct.toVO(userDetail);
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

        log.info("更新用户详情成功，用户ID：{}", userId);
        return getUserDetailByUserId(userId);
    }
}
