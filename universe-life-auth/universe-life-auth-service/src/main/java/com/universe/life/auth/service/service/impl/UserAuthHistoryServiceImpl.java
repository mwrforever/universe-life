package com.universe.life.auth.service.service.impl;

import com.universe.life.auth.service.domain.po.UserAuthHistory;
import com.universe.life.auth.service.mapper.UserAuthHistoryMapper;
import com.universe.life.auth.service.service.IUserAuthHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户认证历史表：记录登录、登出等认证事件 服务实现类
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
@Service
public class UserAuthHistoryServiceImpl extends ServiceImpl<UserAuthHistoryMapper, UserAuthHistory> implements IUserAuthHistoryService {

}
