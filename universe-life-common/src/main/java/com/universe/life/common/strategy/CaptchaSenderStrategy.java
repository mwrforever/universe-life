package com.universe.life.common.strategy;

import com.universe.life.common.enums.CaptchaUsageType;

/**
 * @author 毛伟然
 * @since 2025/11/16 11:02
 */
public interface CaptchaSenderStrategy {

    boolean support(String identification);

    void send(String identification, String captcha);

    default void send(String identification, String captcha, CaptchaUsageType usageType) {
        send(identification, captcha);
    }
}
