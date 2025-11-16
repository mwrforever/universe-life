package com.universe.life.common.util;

import cn.hutool.core.lang.Validator;
import com.universe.life.common.strategy.CaptchaSenderStrategy;

/**
 * @author 毛伟然
 * @since 2025/11/16 15:02
 */
public class EmailCaptchaSender implements CaptchaSenderStrategy {
    @Override
    public boolean support(String identification) {
        return Validator.isEmail(identification);
    }

    @Override
    public void send(String identification, String captcha) {
    }
}
