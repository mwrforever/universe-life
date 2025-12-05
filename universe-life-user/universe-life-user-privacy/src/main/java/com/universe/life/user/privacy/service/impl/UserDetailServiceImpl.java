package com.universe.life.user.privacy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.universe.life.user.privacy.domain.po.UserDetail;
import com.universe.life.user.privacy.mapper.UserDetailMapper;
import com.universe.life.user.privacy.mapstruct.UserDetailMapstruct;
import com.universe.life.user.privacy.service.IUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户详情表 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl extends ServiceImpl<UserDetailMapper, UserDetail> implements IUserDetailService {

    private final UserDetailMapstruct userDetailMapstruct;

}
