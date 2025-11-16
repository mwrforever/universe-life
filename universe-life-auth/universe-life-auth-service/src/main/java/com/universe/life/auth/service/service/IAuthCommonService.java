package com.universe.life.auth.service.service;

import com.universe.life.auth.resource.domain.dto.request.CaptchaRequest;
import com.universe.life.auth.resource.domain.vo.CaptchaVO;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:09
 */
public interface IAuthCommonService {

    /**
     * 发送验证码
     *
     * @param request 验证码请求参数
     * @return 验证码响应结果
     */
    CaptchaVO sendCaptcha(CaptchaRequest request);
}
