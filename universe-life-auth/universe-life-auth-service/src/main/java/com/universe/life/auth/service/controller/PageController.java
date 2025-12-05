package com.universe.life.auth.service.controller;

import com.universe.life.auth.service.domain.vo.DisclaimerVO;
import com.universe.life.auth.service.domain.vo.PrivacyPolicyVO;
import com.universe.life.auth.service.domain.vo.UserAgreementVO;
import com.universe.life.auth.service.service.IAuthCommonService;
import com.universe.life.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 页面控制器
 * 负责处理页面跳转和Thymeleaf模板渲染
 *
 * @author 毛伟然
 * @since 2025/11/21
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class PageController {

    private final IAuthCommonService authCommonService;

    /**
     * 登录页面
     */
    @GetMapping({"/", "/login"})
    public String login(Model model, HttpServletRequest request) {
        // 添加常用页面数据
        model.addAttribute("title", "万象生活 - 登录注册");
        model.addAttribute("appName", "万象生活");
        model.addAttribute("appDescription", "连接您的生活，创造无限可能");

        // 记录请求参数，便于调试OAuth2重定向问题
        String clientId = request.getParameter("client_id");
        String redirectUri = request.getParameter("redirect_uri");
        String responseType = request.getParameter("response_type");
        String scope = request.getParameter("scope");
        String state = request.getParameter("state");

        if (clientId != null || redirectUri != null) {
            System.out.println("OAuth2登录请求参数 - client_id: " + clientId + ", redirect_uri: " + redirectUri +
                ", response_type: " + responseType + ", scope: " + scope + ", state: " + state);
        }

        return "login"; // 恢复到正常的登录模板
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "万象生活 - 用户注册");
        model.addAttribute("appName", "万象生活");
        model.addAttribute("appDescription", "连接您的生活，创造无限可能");
        return "register"; // 需要创建register.html模板
    }

    /**
     * 注册信息填写页面
     */
    @GetMapping("/register-info")
    public String registerInfo(Model model) {
        model.addAttribute("title", "万象生活 - 完善注册信息");
        model.addAttribute("appName", "万象生活");
        model.addAttribute("appDescription", "连接您的生活，创造无限可能");
        return "register-info"; // 需要创建register-info.html模板
    }

    /**
     * 获取用户服务协议
     */
    @GetMapping("/user-agreement")
    @ResponseBody
    @Operation(
            summary = "获取用户服务协议",
            description = "获取用户服务协议内容"
    )
    public Result<UserAgreementVO> getUserAgreement() {
        return Result.success(authCommonService.getUserAgreement());
    }

    /**
     * 获取隐私政策
     */
    @GetMapping("/privacy-policy")
    @ResponseBody
    @Operation(
            summary = "获取隐私政策",
            description = "获取隐私政策内容"
    )
    public Result<PrivacyPolicyVO> getPrivacyPolicy() {
        return Result.success(authCommonService.getPrivacyPolicy());
    }

    /**
     * 获取平台免责声明
     */
    @GetMapping("/disclaimer")
    @ResponseBody
    @Operation(
            summary = "获取平台免责声明",
            description = "获取平台免责声明内容"
    )
    public Result<DisclaimerVO> getDisclaimer() {
        return Result.success(authCommonService.getDisclaimer());
    }

}