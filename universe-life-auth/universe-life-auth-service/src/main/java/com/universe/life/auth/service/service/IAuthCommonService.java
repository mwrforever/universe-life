package com.universe.life.auth.service.service;

import com.universe.life.auth.resource.domain.dto.request.CaptchaRequest;
import com.universe.life.auth.resource.domain.dto.request.VerifyCodeFormRequest;
import com.universe.life.auth.resource.domain.vo.CaptchaVO;
import com.universe.life.auth.service.domain.vo.DisclaimerVO;
import com.universe.life.auth.service.domain.vo.PrivacyPolicyVO;
import com.universe.life.auth.service.domain.vo.UserAgreementVO;

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
    void sendCaptcha(CaptchaRequest request);

    /**
     * 验证验证码
     *
     * @param request 验证码请求参数
     * @return 验证码响应结果
     */
    CaptchaVO verifyCaptcha(VerifyCodeFormRequest request);

    /**
     * 获取用户服务协议
     *
     * @return 用户服务协议VO
     */
    UserAgreementVO getUserAgreement();

    /**
     * 获取隐私政策
     *
     * @return 隐私政策VO
     */
    PrivacyPolicyVO getPrivacyPolicy();

    /**
     * 获取平台免责声明
     *
     * @return 平台免责声明VO
     */
    DisclaimerVO getDisclaimer();
}
