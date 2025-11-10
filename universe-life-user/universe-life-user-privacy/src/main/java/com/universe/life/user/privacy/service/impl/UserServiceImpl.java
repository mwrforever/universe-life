package com.universe.life.user.privacy.service.impl;

import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.mapper.UserMapper;
import com.universe.life.user.privacy.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-03
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
