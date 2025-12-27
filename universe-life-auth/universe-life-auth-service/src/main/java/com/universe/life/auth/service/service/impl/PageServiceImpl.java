package com.universe.life.auth.service.service.impl;

import com.universe.life.auth.service.domain.vo.DisclaimerVO;
import com.universe.life.auth.service.domain.vo.PrivacyPolicyVO;
import com.universe.life.auth.service.domain.vo.UserAgreementVO;
import com.universe.life.auth.service.service.IPageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @author 毛伟然
 * @since 2025/12/21 13:18
 */
@Slf4j
@Service
public class PageServiceImpl implements IPageService {


    @Override
    public UserAgreementVO getUserAgreement() {
        String content = readTermsFile("user-agreement.html");
        return new UserAgreementVO(content);
    }

    @Override
    public PrivacyPolicyVO getPrivacyPolicy() {
        String content = readTermsFile("privacy-policy.html");
        return new PrivacyPolicyVO(content);
    }

    @Override
    public DisclaimerVO getDisclaimer() {
        String content = readTermsFile("disclaimer.html");
        return new DisclaimerVO(content);
    }

    /**
     * 读取协议文件内容
     *
     * @param fileName 文件名
     * @return 文件内容
     */
    private String readTermsFile(String fileName) {
        try {
            // 从类路径读取静态资源文件
            Resource resource = new ClassPathResource("static/terms/" + fileName);
            if (resource.exists()) {
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            return getFallbackTermsContent(fileName);
        } catch (Exception e) {
            log.error("读取协议文件失败: {}", fileName, e);
            return getFallbackTermsContent(fileName);
        }
    }

    /**
     * 获取备用协议内容
     *
     * @param fileName 文件名
     * @return 备用内容
     */
    private String getFallbackTermsContent(String fileName) {
        if (fileName.contains("user-agreement")) {
            return "<h4>用户服务协议</h4><p>欢迎使用万象生活平台。本协议是您与万象生活平台之间关于使用本服务的法律协议...</p>";
        } else if (fileName.contains("privacy-policy")) {
            return "<h4>隐私政策</h4><p>万象生活非常重视您的隐私保护。本隐私政策说明了我们如何收集、使用和保护您的个人信息...</p>";
        } else if (fileName.contains("disclaimer")) {
            return "<h4>平台免责声明</h4><p>万象生活平台作为信息服务平台，在此声明以下免责条款...</p>";
        }
        return "<p>协议内容加载中...</p>";
    }

}
