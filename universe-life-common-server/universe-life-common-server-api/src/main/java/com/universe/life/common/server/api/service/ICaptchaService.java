package com.universe.life.common.server.api.service;


import com.universe.life.common.server.model.domain.domain.dto.request.CaptchaRequest;
import com.universe.life.common.server.model.domain.domain.dto.request.VerifyCodeFormRequest;
import com.universe.life.common.server.model.domain.domain.vo.CaptchaVO;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:09
 */
public interface ICaptchaService {

    /**
     * 发送验证码
     *
     * @param request 验证码请求参数
     */
    void sendCaptcha(CaptchaRequest request);

    /**
     * 验证验证码
     *
     * @param request 验证码请求参数
     * @return 验证码响应结果
     */
    CaptchaVO verifyCaptcha(VerifyCodeFormRequest request);


}
