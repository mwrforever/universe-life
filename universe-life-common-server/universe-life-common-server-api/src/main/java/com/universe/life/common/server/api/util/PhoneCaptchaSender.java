package com.universe.life.common.server.api.util;

import cn.hutool.core.lang.Validator;
import com.universe.life.common.server.api.strategy.CaptchaSenderStrategy;


/**
 * @author 毛伟然
 * @since 2025/11/16 11:25
 */
public class PhoneCaptchaSender implements CaptchaSenderStrategy {
    @Override
    public boolean support(String identification) {
        return Validator.isMobile(identification);
    }

    @Override
    public void send(String identification, String captcha) {

    }
}
