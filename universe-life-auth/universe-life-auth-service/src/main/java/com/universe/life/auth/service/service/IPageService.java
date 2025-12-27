package com.universe.life.auth.service.service;

import com.universe.life.auth.service.domain.vo.DisclaimerVO;
import com.universe.life.auth.service.domain.vo.PrivacyPolicyVO;
import com.universe.life.auth.service.domain.vo.UserAgreementVO;

/**
 * @author 毛伟然
 * @since 2025/12/21 13:18
 */
public interface IPageService {

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
