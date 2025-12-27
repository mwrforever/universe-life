package com.universe.life.auth.resource.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.universe.life.api.client.UserClient;
import com.universe.life.auth.common.constants.RedisConstants;
import com.universe.life.auth.common.domain.dto.UserAuthInfo;
import com.universe.life.model.domain.dto.UserInfoDTO;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/4 10:48
 */
@AllArgsConstructor
public class UserAuthInfoService implements UserDetailsService {

    private final UserClient userClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库中获取用户信息
        UserInfoDTO userInfo = userClient.getUserInfo(username);
        if (userInfo == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        // 封装用户信息
        UserAuthInfo userAuthInfo = new UserAuthInfo(userInfo.getId(), userInfo.getUsername(), userInfo.getPassword(), userInfo.getAvatar());
        // 写入缓存
        String userInfoKey = RedisConstants.USER_AUTH_UID_KEY + userInfo.getId();
        stringRedisTemplate.opsForHash().put(
                userInfoKey,
                RedisConstants.AUTH_USER_DATA,
                JSONUtil.toJsonStr(userAuthInfo)
        );
        stringRedisTemplate.opsForHash().put(
                userInfoKey,
                RedisConstants.AUTH_USER_TYPE,
                "user"
        );
        // 设置过期时间
        stringRedisTemplate.expire(
                userInfoKey,
                RedisConstants.AUTH_USER_CAPTCHA_EXPIRE_TIME + RandomUtil.randomInt(10, 30),
                TimeUnit.MINUTES
        );
        return userAuthInfo;
    }


}
