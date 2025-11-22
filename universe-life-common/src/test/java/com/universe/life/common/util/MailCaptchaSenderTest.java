package com.universe.life.common.util;

import com.universe.life.common.domain.dto.MailCaptchaDTO;
import com.universe.life.common.enums.CaptchaUsageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MailCaptchaSender 测试类
 *
 * @author 毛伟然
 * @since 2025/11/17
 */
@ExtendWith(MockitoExtension.class)
class MailCaptchaSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private com.universe.life.common.properties.MailProperties mailProperties;

    @InjectMocks
    private MailCaptchaSender mailCaptchaSender;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CAPTCHA = "123456";
    private static final String FROM_EMAIL = "noreply@universe-life.com";
    private static final String APP_NAME = "Universe Life";
    private static final String APP_URL = "http://127.0.0.1:3000";

    @BeforeEach
    void setUp() {
        // 配置Mock属性
        when(mailProperties.getFrom()).thenReturn(FROM_EMAIL);
        when(mailProperties.getAppName()).thenReturn(APP_NAME);
        when(mailProperties.getAppUrl()).thenReturn(APP_URL);
    }

    @Test
    void testSupport_ValidEmail_ReturnsTrue() {
        assertTrue(mailCaptchaSender.support(TEST_EMAIL));
    }

    @Test
    void testSupport_InvalidEmail_ReturnsFalse() {
        assertFalse(mailCaptchaSender.support("invalid-email"));
        assertFalse(mailCaptchaSender.support(""));
        assertFalse(mailCaptchaSender.support(null));
    }

    @Test
    void testSend_WithValidParameters_ShouldSendEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String expectedHtmlContent = "<html>test content</html>";
        when(templateEngine.process(eq("mail/captcha-notification"), any(Context.class)))
                .thenReturn(expectedHtmlContent);

        // When
        mailCaptchaSender.send(TEST_EMAIL, TEST_CAPTCHA);

        // Then
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("mail/captcha-notification"), any(Context.class));
    }

    @Test
    void testSendHtmlCaptcha_WithCompleteData_ShouldSendEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                .setToEmail(TEST_EMAIL)
                .setCaptcha(TEST_CAPTCHA)
                .setUsageType(CaptchaUsageType.REGISTER)
                .setExpirationMinutes(10);

        String expectedHtmlContent = "<html>registration content</html>";
        when(templateEngine.process(eq("mail/captcha-notification"), any(Context.class)))
                .thenReturn(expectedHtmlContent);

        // When
        mailCaptchaSender.sendHtmlCaptcha(captchaDTO);

        // Then
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("mail/captcha-notification"), any(Context.class));

        // 验证Context变量
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("mail/captcha-notification"), contextCaptor.capture());

        Context context = contextCaptor.getValue();
        assertEquals(TEST_CAPTCHA, context.getVariable("captcha"));
        assertEquals(CaptchaUsageType.REGISTER, context.getVariable("usageType"));
        assertEquals(10, context.getVariable("expirationMinutes"));
    }

    @Test
    void testSendHtmlCaptcha_WithInvalidEmail_ShouldThrowException() {
        // Given
        MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                .setToEmail("invalid-email")
                .setCaptcha(TEST_CAPTCHA);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> mailCaptchaSender.sendHtmlCaptcha(captchaDTO)
        );

        assertTrue(exception.getMessage().contains("邮件发送失败"));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getCause().getMessage().contains("邮箱地址格式不正确"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendHtmlCaptcha_WithCustomContent_ShouldUseCustomContent() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String customContent = "<h1>Custom HTML Content</h1>";
        MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                .setToEmail(TEST_EMAIL)
                .setCaptcha(TEST_CAPTCHA)
                .setCustomContent(customContent);

        // When
        mailCaptchaSender.sendHtmlCaptcha(captchaDTO);

        // Then
        verify(mailSender).send(mimeMessage);
        verify(templateEngine, never()).process(any(String.class), any(Context.class));
    }

    @Test
    void testSendHtmlCaptcha_WithCustomSubject_ShouldUseCustomSubject() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String customSubject = "自定义验证码邮件";
        MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                .setToEmail(TEST_EMAIL)
                .setCaptcha(TEST_CAPTCHA)
                .setCustomSubject(customSubject);

        when(templateEngine.process(eq("mail/captcha-notification"), any(Context.class)))
                .thenReturn("<html>test</html>");

        // When
        mailCaptchaSender.sendHtmlCaptcha(captchaDTO);

        // Then
        verify(mailSender).send(mimeMessage);
        // 注意：这里我们无法直接验证主题，因为MimeMessageHelper的行为
        // 在集成测试中会更准确地验证
    }

    @Test
    void testSendCustomHtmlEmail_WithValidParameters_ShouldSendEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String subject = "测试邮件";
        String htmlContent = "<html><body><h1>测试内容</h1></body></html>";

        // When
        mailCaptchaSender.sendCustomHtmlEmail(TEST_EMAIL, subject, htmlContent);

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendCustomHtmlEmail_WithInvalidEmail_ShouldThrowException() {
        // Given
        String subject = "测试邮件";
        String htmlContent = "<html><body><h1>测试内容</h1></body></html>";

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> mailCaptchaSender.sendCustomHtmlEmail("invalid-email", subject, htmlContent)
        );

        assertTrue(exception.getMessage().contains("邮件发送失败"));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getCause().getMessage().contains("邮箱地址格式不正确"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testCreateCaptchaDTO_ShouldCreateValidDTO() {
        // Given
        CaptchaUsageType usageType = CaptchaUsageType.LOGIN;

        // When
        MailCaptchaDTO result = mailCaptchaSender.createCaptchaDTO(TEST_EMAIL, TEST_CAPTCHA, usageType);

        // Then
        assertEquals(TEST_EMAIL, result.getToEmail());
        assertEquals(TEST_CAPTCHA, result.getCaptcha());
        assertEquals(usageType, result.getUsageType());
        assertEquals(APP_URL, result.getPlatformUrl());
    }

    @Test
    void testSendHtmlCaptcha_WhenMailSendFails_ShouldThrowRuntimeException() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                .setToEmail(TEST_EMAIL)
                .setCaptcha(TEST_CAPTCHA);

        when(templateEngine.process(eq("mail/captcha-notification"), any(Context.class)))
                .thenReturn("<html>test</html>");

        doThrow(new RuntimeException("邮件服务器连接失败"))
                .when(mailSender).send(any(MimeMessage.class));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> mailCaptchaSender.sendHtmlCaptcha(captchaDTO)
        );

        assertTrue(exception.getMessage().contains("邮件发送失败"));
        assertEquals("邮件服务器连接失败", exception.getCause().getMessage());
    }

    @Test
    void testSendHtmlCaptcha_DifferentUsageTypes_ShouldGenerateCorrectSubjects() {
        // 这个测试需要在集成测试中验证，因为主题生成在MimeMessageHelper中
        // 这里我们验证不同usageType都能正确处理
        CaptchaUsageType[] usageTypes = CaptchaUsageType.values();

        for (CaptchaUsageType usageType : usageTypes) {
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            MailCaptchaDTO captchaDTO = new MailCaptchaDTO()
                    .setToEmail(TEST_EMAIL)
                    .setCaptcha(TEST_CAPTCHA)
                    .setUsageType(usageType);

            when(templateEngine.process(eq("mail/captcha-notification"), any(Context.class)))
                    .thenReturn("<html>test</html>");

            assertDoesNotThrow(() -> mailCaptchaSender.sendHtmlCaptcha(captchaDTO));
        }
    }
}