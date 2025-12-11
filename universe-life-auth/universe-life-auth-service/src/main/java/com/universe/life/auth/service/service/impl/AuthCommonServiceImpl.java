package com.universe.life.auth.service.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.universe.life.auth.resource.domain.dto.request.CaptchaRequest;
import com.universe.life.auth.resource.domain.dto.request.VerifyCodeFormRequest;
import com.universe.life.auth.resource.domain.vo.CaptchaVO;
import com.universe.life.auth.resource.constants.RedisConstants;
import com.universe.life.auth.resource.util.VerifyCaptchaUtil;
import com.universe.life.auth.service.domain.vo.DisclaimerVO;
import com.universe.life.auth.service.domain.vo.PrivacyPolicyVO;
import com.universe.life.auth.service.domain.vo.UserAgreementVO;
import com.universe.life.auth.service.service.IAuthCommonService;
import com.universe.life.common.enums.CaptchaUsageType;
import com.universe.life.auth.common.exception.BusinessException;
import com.universe.life.auth.common.message.ExceptionMessage;
import com.universe.life.common.strategy.CaptchaSenderStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 毛伟然
 * @since 2025/11/15 21:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCommonServiceImpl implements IAuthCommonService {

    private final StringRedisTemplate stringRedisTemplate;

    private final List<CaptchaSenderStrategy> captchaSenderStrategies;

    private final VerifyCaptchaUtil verifyCaptchaUtil;

    @Override
    public CaptchaVO verifyCaptcha(VerifyCodeFormRequest request) {
        boolean verified = verifyCaptchaUtil.verifyCaptcha(request.getCaptchaUsageType(), request.getIdentification(), request.getVerifyCode());
        if (!verified) {
            throw new BusinessException.ParamException(ExceptionMessage.CAPTCHA_ERROR);
        }
        // 返货验证标识VO
        if (hasCheckIssuer(request.getCaptchaUsageType())) {
            String issuer = UUID.randomUUID().toString().replace("-", "");
            String key =
                    RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX +
                            request.getCaptchaUsageType().getDisplayName() + ":" +
                            request.getIdentification() + ":" +
                            RedisConstants.AUTH_ISSUER;
            stringRedisTemplate.opsForValue().set(key, issuer, 15, TimeUnit.MINUTES);
            return new CaptchaVO(issuer);
        }
        return null;
    }

    private boolean hasCheckIssuer(CaptchaUsageType usageType) {
        return !usageType.equals(CaptchaUsageType.LOGIN);
    }

    @Override
    public void sendCaptcha(CaptchaRequest request) {
        // 先查看redis中是否有验证码
        String defendKey = RedisConstants.AUTH_USER_CAPTCHA_LOCK
                + request.getCaptchaUsageType().getDisplayName() + ":" +
                request.getIdentification();
        Boolean hasKey = stringRedisTemplate.hasKey(defendKey);
        if (hasKey) {
            throw new BusinessException.DataAlreadyExistsException(ExceptionMessage.CAPTCHA_ALREADY_EXISTS);
        }
        // 生成验证码
        String captcha = RandomUtil.randomNumbers(6);
        // 发送验证码
        captchaSenderStrategies.stream()
                .filter(captchaSenderStrategy -> captchaSenderStrategy.support(request.getIdentification()))
                .findFirst()
                .ifPresentOrElse(
                        captchaSenderStrategy -> captchaSenderStrategy.send(request.getIdentification(), captcha, request.getCaptchaUsageType()),
                        () -> {
                            throw new BusinessException.ParamException(ExceptionMessage.PHONE_EMAIL_FORMAT_ERROR);
                        }
                );
        // 缓存验证码
        String key =
                RedisConstants.AUTH_USER_CAPTCHA_KEY_PREFIX +
                        request.getCaptchaUsageType().getDisplayName() + ":" +
                        request.getIdentification() + ":" +
                        RedisConstants.AUTH_IDENTIFICATION;

        stringRedisTemplate.opsForValue().set(key, captcha, 15, TimeUnit.MINUTES);
        // 缓存验证码防刷时间
        stringRedisTemplate.opsForValue().setIfAbsent(
                defendKey,
                "1", 1, TimeUnit.MINUTES);
    }

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
