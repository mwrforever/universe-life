package com.universe.life.user.privacy.listener;

import com.universe.life.auth.common.constants.JwtConstants;
import com.universe.life.auth.common.constants.RabbitMqConstants;
import com.universe.life.auth.common.domain.message.LoginInfoMessage;
import com.universe.life.user.privacy.domain.po.SysUser;
import com.universe.life.user.privacy.domain.po.User;
import com.universe.life.user.privacy.service.ISysUserService;
import com.universe.life.user.privacy.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 登录信息消息监听器
 * 消费登录信息消息并更新用户/员工的登录记录
 *
 * @author universe-life
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInfoListener {

    private final IUserService userService;
    private final ISysUserService sysUserService;

    /**
     * 处理登录信息消息
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = RabbitMqConstants.Queue.USER_LOGIN_INFO_QUEUE, durable = "true", autoDelete = "false"),
                    exchange = @Exchange(name = RabbitMqConstants.Exchange.USER_NOTIFY_EXCHANGE, type = ExchangeTypes.TOPIC),
                    key = RabbitMqConstants.RoutingKey.USER_LOGIN_INFO_ROUTING_KEY
            )
    )
    public void handleLoginInfo(LoginInfoMessage message) {
        log.info("收到登录信息消息: userId={}, loginType={}, loginIp={}",
                message.getUserId(), message.getLoginType(), message.getLoginIp());

        try {
            if (JwtConstants.USER_LOGIN.equals(message.getLoginType())) {
                updateUserLoginInfo(message);
            } else if (JwtConstants.EMPLOYEE_LOGIN.equals(message.getLoginType())) {
                updateSysUserLoginInfo(message);
            } else {
                log.warn("未知的登录类型: {}", message.getLoginType());
            }
        } catch (Exception e) {
            log.error("处理登录信息消息失败: {}", message, e);
        }
    }

    /**
     * 更新用户登录信息
     */
    private void updateUserLoginInfo(LoginInfoMessage message) {
        try {
            boolean updated = userService.lambdaUpdate()
                    .set(User::getLastLoginIp, message.getLoginIp())
                    .set(User::getLastLoginAt, message.getLoginTime())
                    .eq(User::getId, message.getUserId())
                    .update();
            if (updated) {
                log.debug("用户登录信息更新成功: userId={}", message.getUserId());
            } else {
                log.error("用户登录信息更新失败: userId={}", message.getUserId());
            }
        } catch (Exception e) {
            log.error("更新用户登录信息异常: userId={}", message.getUserId(), e);
        }
    }

    /**
     * 更新员工登录信息
     */
    private void updateSysUserLoginInfo(LoginInfoMessage message) {
        try {
            boolean updated = sysUserService.lambdaUpdate()
                    .set(SysUser::getLastLoginIp, message.getLoginIp())
                    .set(SysUser::getLastLoginAt, message.getLoginTime())
                    .eq(SysUser::getId, message.getUserId())
                    .update();
            if (updated) {
                log.debug("员工登录信息更新成功: userId={}", message.getUserId());
            } else {
                log.error("员工登录信息更新失败: userId={}", message.getUserId());
            }
        } catch (Exception e) {
            log.error("更新员工登录信息异常: userId={}", message.getUserId(), e);
        }
    }
}
