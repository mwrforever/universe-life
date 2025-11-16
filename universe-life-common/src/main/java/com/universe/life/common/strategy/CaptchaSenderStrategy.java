package com.universe.life.common.strategy;

/**
 * @author 毛伟然
 * @since 2025/11/16 11:02
 */
public interface CaptchaSenderStrategy {

    boolean support(String identification);

    void send(String identification, String captcha);

}
