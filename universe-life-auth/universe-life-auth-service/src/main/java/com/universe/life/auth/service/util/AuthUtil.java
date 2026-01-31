package com.universe.life.auth.service.util;

import cn.hutool.core.util.StrUtil;
import com.universe.life.auth.common.constants.RabbitMqConstants;
import com.universe.life.auth.common.domain.dto.AdminAuthInfo;
import com.universe.life.auth.common.domain.dto.UserAuthInfo;
import com.universe.life.auth.common.domain.message.LoginInfoMessage;
import com.universe.life.common.util.RabbitMqSender;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.alibaba.nacos.plugin.auth.constant.Constants.Identity.X_REAL_IP;
import static com.google.common.net.HttpHeaders.X_FORWARDED_FOR;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final RabbitMqSender rabbitMqSender;

    /**
     * 提取客户端真实IP地址
     * 优先级: X-Forwarded-For > X-Real-IP > RemoteAddr
     */
    public String extractClientIp(HttpServletRequest request) {
        // 优先检查 X-Forwarded-For header（代理场景）
        String ip = request.getHeader(X_FORWARDED_FOR);
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For 可能包含多个IP，取第一个
            int index = ip.indexOf(',');
            if (index > 0) {
                ip = ip.substring(0, index).trim();
            }
            return ip;
        }

        // 其次检查 X-Real-IP header
        ip = request.getHeader(X_REAL_IP);
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        // 最后使用 RemoteAddr
        return request.getRemoteAddr();
    }


    /**
     * 异步发送登录信息消息
     */
    public void sendLoginInfoAsync(UserDetails userDetails, String loginType, String loginIp) {
        try {
            Long userId = extractUserId(userDetails);
            if (userId == null) {
                log.warn("无法提取用户ID，跳过发送登录信息消息");
                return;
            }

            LoginInfoMessage message = LoginInfoMessage.builder()
                    .userId(userId)
                    .loginIp(loginIp)
                    .loginTime(LocalDateTime.now())
                    .loginType(loginType)
                    .build();

            rabbitMqSender.builder()
                    .to(RabbitMqConstants.Exchange.USER_NOTIFY_EXCHANGE,
                            RabbitMqConstants.RoutingKey.USER_LOGIN_INFO_ROUTING_KEY)
                    .persistent(true)
                    .sendAsync(message);

            log.debug("登录信息消息已发送 - userId: {}, loginType: {}", userId, loginType);
        } catch (Exception e) {
            log.error("发送登录信息消息失败", e);
        }
    }

    /**
     * 从 UserDetails 中提取用户ID
     */
    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof UserAuthInfo) {
            return ((UserAuthInfo) userDetails).getId();
        } else if (userDetails instanceof AdminAuthInfo) {
            return ((AdminAuthInfo) userDetails).getId();
        }
        return null;
    }
}
