package com.universe.life.user.privacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.universe.life.user.privacy.domain.po.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper 接口
 *
 * @author 毛伟然
 * @since 2025-11-13
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
