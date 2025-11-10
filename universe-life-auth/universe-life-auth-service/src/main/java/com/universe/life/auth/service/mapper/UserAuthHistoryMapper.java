package com.universe.life.auth.service.mapper;

import com.universe.life.auth.service.domain.po.UserAuthHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 用户认证历史表：记录登录、登出等认证事件 Mapper 接口
 * </p>
 *
 * @author 毛伟然
 * @since 2025-11-10
 */
public interface UserAuthHistoryMapper extends BaseMapper<UserAuthHistory> {

}
