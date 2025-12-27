// 万象生活登录页面逻辑

// API 基础地址配置 - 公共服务接口必须通过网关访问
const GATEWAY_BASE_URL = 'http://localhost:8101';

class LoginAuth {
    constructor() {
        this.currentLoginTab = 'password';
        this.countdownTimer = null;
        this.countdownTime = 60;
        this.toastContainer = null;
        this.csrfToken = null;
        this.csrfHeader = null;
        this.init();
    }

    // 获取CSRF Token
    getCsrfToken() {
        if (this.csrfToken && this.csrfHeader) {
            return { token: this.csrfToken, header: this.csrfHeader };
        }

        // 从meta标签获取CSRF token（Thymeleaf自动注入）
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

        if (csrfMeta && csrfHeaderMeta) {
            this.csrfToken = csrfMeta.getAttribute('content');
            this.csrfHeader = csrfHeaderMeta.getAttribute('content');
            return { token: this.csrfToken, header: this.csrfHeader };
        }

        return null;
    }

    // 为请求添加CSRF Headers
    addCsrfHeaders(headers = {}) {
        const csrf = this.getCsrfToken();
        if (csrf && csrf.token && csrf.header) {
            headers[csrf.header] = csrf.token;
        }
        return headers;
    }

    init() {
        // 初始化协议加载器
        this.termsLoader = new TermsLoader();

        // 清理所有表单内容
        this.clearAllForms();

        this.setupEventListeners();
        this.setupFormValidation();
        this.setupModal();
        this.createFloatingIcons();
        this.initToast();
        this.setupPasswordToggle();
    }

    // 初始化Toast容器
    initToast() {
        this.toastContainer = document.createElement('div');
        this.toastContainer.className = 'toast-container';
        document.body.appendChild(this.toastContainer);
    }

    // 显示Toast提示
    showToast(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        const iconMap = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };

        toast.innerHTML = `
            <i class="fas ${iconMap[type] || iconMap.info} toast-icon"></i>
            <div class="toast-message">${message}</div>
            <button class="toast-close">&times;</button>
        `;

        this.toastContainer.appendChild(toast);

        // 显示动画
        setTimeout(() => {
            toast.classList.add('show');
        }, 10);

        // 关闭按钮事件
        const closeBtn = toast.querySelector('.toast-close');
        closeBtn.addEventListener('click', () => {
            this.hideToast(toast);
        });

        // 自动隐藏
        if (duration > 0) {
            setTimeout(() => {
                this.hideToast(toast);
            }, duration);
        }
    }

    // 隐藏Toast
    hideToast(toast) {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }

    // 清理所有表单内容
    clearAllForms() {
        // 清理密码登录表单
        const passwordForm = document.getElementById('password-form');
        if (passwordForm) {
            passwordForm.reset();
            // 清除所有输入框的值
            const inputs = passwordForm.querySelectorAll('input[type="text"], input[type="password"], input[type="checkbox"]');
            inputs.forEach(input => {
                if (input.type === 'checkbox') {
                    input.checked = false;
                } else {
                    input.value = '';
                }
            });
            // 清除所有错误信息
            this.clearFormErrors(passwordForm);
        }

        // 清理邮箱登录表单
        const emailForm = document.getElementById('email-form');
        if (emailForm) {
            emailForm.reset();
            // 清除所有输入框的值
            const inputs = emailForm.querySelectorAll('input[type="email"], input[type="tel"], input[type="checkbox"]');
            inputs.forEach(input => {
                if (input.type === 'checkbox') {
                    input.checked = false;
                } else {
                    input.value = '';
                }
            });
            // 清除所有错误信息
            this.clearFormErrors(emailForm);
        }

        // 重置验证码按钮状态
        const codeButtons = document.querySelectorAll('.verification-btn');
        codeButtons.forEach(btn => {
            btn.disabled = false;
            btn.textContent = '获取验证码';
        });

        // 清除所有Toast提示
        const toasts = document.querySelectorAll('.toast');
        toasts.forEach(toast => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        });
    }

    // 清理表单错误信息
    clearFormErrors(form) {
        const errorMessages = form.querySelectorAll('.error-message');
        errorMessages.forEach(error => {
            error.classList.remove('show');
            error.textContent = '';
        });

        const inputs = form.querySelectorAll('input');
        inputs.forEach(input => {
            input.style.borderColor = '';
        });
    }

    // 创建浮动背景图标
    createFloatingIcons() {
        const floatingIcons = document.querySelector('.floating-icons');
        if (!floatingIcons) return;

        const icons = ['🏠', '🛍️', '🍔', '💇', '🏋️', '⭐'];
        icons.forEach((icon, index) => {
            const iconElement = document.createElement('div');
            iconElement.className = 'floating-icon';
            iconElement.textContent = icon;
            iconElement.style.animationDelay = `${index}s`;
            floatingIcons.appendChild(iconElement);
        });
    }

    // 设置事件监听器
    setupEventListeners() {
        // 登录标签切换
        document.querySelectorAll('.tab-button').forEach(tab => {
            tab.addEventListener('click', (e) => {
                const targetTab = e.target.dataset.tab;
                this.switchLoginTab(targetTab);
            });
        });

        // 表单提交事件
        this.setupFormSubmit();

        // 验证码按钮
        document.querySelectorAll('.verification-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.sendVerificationCode(btn);
            });
        });

        // 社交登录按钮
        document.querySelectorAll('.social-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.handleSocialLogin(e.target);
            });
        });
    }

    // 切换登录标签页
    switchLoginTab(tab) {
        // 清理当前表单的错误和内容
        this.clearCurrentTabForm();

        // 更新标签按钮状态
        document.querySelectorAll('.tabs-header .tab-button').forEach(btn => {
            btn.classList.remove('active');
            if (btn.dataset.tab === tab) {
                btn.classList.add('active');
            }
        });

        // 切换内容
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
        });

        document.getElementById(`${tab}-tab`).classList.add('active');

        this.currentLoginTab = tab;
    }

    // 清理当前标签页的表单
    clearCurrentTabForm() {
        // 清理密码登录表单
        if (this.currentLoginTab === 'password') {
            const passwordForm = document.getElementById('password-form');
            if (passwordForm) {
                passwordForm.reset();
                const inputs = passwordForm.querySelectorAll('input[type="text"], input[type="password"], input[type="checkbox"]');
                inputs.forEach(input => {
                    if (input.type === 'checkbox') {
                        input.checked = false;
                    } else {
                        input.value = '';
                    }
                });
                this.clearFormErrors(passwordForm);
            }
        }

        // 清理邮箱登录表单
        if (this.currentLoginTab === 'email') {
            const emailForm = document.getElementById('email-form');
            if (emailForm) {
                emailForm.reset();
                const inputs = emailForm.querySelectorAll('input[type="email"], input[type="tel"], input[type="checkbox"]');
                inputs.forEach(input => {
                    if (input.type === 'checkbox') {
                        input.checked = false;
                    } else {
                        input.value = '';
                    }
                });
                this.clearFormErrors(emailForm);
            }

            // 重置验证码按钮状态
            const sendCodeBtn = document.getElementById('login-send-code-btn');
            if (sendCodeBtn) {
                sendCodeBtn.disabled = false;
                sendCodeBtn.textContent = '获取验证码';
            }
        }
    }

    // 设置表单验证
    setupFormValidation() {
        // 用户名验证
        const usernameInput = document.getElementById('username');
        if (usernameInput) {
            usernameInput.addEventListener('blur', () => this.validateUsername(usernameInput));
            usernameInput.addEventListener('input', () => this.clearError(usernameInput));
        }

        // 邮箱验证
        const emailInput = document.getElementById('email');
        if (emailInput) {
            emailInput.addEventListener('blur', () => this.validateEmail(emailInput));
            emailInput.addEventListener('input', () => this.clearError(emailInput));
        }

        // 验证码验证
        const emailVerificationCodeInput = document.getElementById('email-verification-code');
        if (emailVerificationCodeInput) {
            emailVerificationCodeInput.addEventListener('blur', () => this.validateVerificationCode(emailVerificationCodeInput));
            emailVerificationCodeInput.addEventListener('input', () => this.clearError(emailVerificationCodeInput));
        }

        // 密码验证
        const passwordInputs = document.querySelectorAll('#password');
        passwordInputs.forEach(input => {
            input.addEventListener('blur', () => this.validatePassword(input));
            input.addEventListener('input', () => this.clearError(input));
        });
    }

    // 验证用户名（支持用户名或邮箱）
    validateUsername(input) {
        const value = input.value.trim();
        if (!value) {
            this.showError(input, '请输入用户名或邮箱');
            return false;
        }

        // 检查是否为邮箱格式
        if (value.includes('@')) {
            // 邮箱格式验证
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(value)) {
                this.showError(input, '请输入有效的邮箱地址');
                return false;
            }
            if (value.length > 50) {
                this.showError(input, '邮箱地址长度不能超过50个字符');
                return false;
            }
        } else {
            // 用户名格式验证
            if (value.length < 3 || value.length > 20) {
                this.showError(input, '用户名长度应在3-20个字符之间');
                return false;
            }
            if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(value)) {
                this.showError(input, '用户名只能包含中文、英文、数字、下划线');
                return false;
            }
        }

        this.clearError(input);
        return true;
    }

    // 验证邮箱
    validateEmail(input) {
        const value = input.value.trim();
        if (!value) {
            this.showError(input, '请输入邮箱地址');
            return false;
        }
        if (!value.includes('@')) {
            this.showError(input, '请输入有效的邮箱地址');
            return false;
        }
        this.clearError(input);
        return true;
    }

    // 验证密码
    validatePassword(input) {
        const value = input.value;
        if (!value) {
            this.showError(input, '请输入密码');
            return false;
        }
        if (value.length < 6 || value.length > 20) {
            this.showError(input, '密码长度应在6-20个字符之间');
            return false;
        }
        if (/\s/.test(value)) {
            this.showError(input, '密码不能包含空格字符');
            return false;
        }
        this.clearError(input);
        return true;
    }

    // 验证验证码
    validateVerificationCode(input) {
        const value = input.value.trim();
        if (!value) {
            this.showError(input, '请输入验证码');
            return false;
        }
        // 验证是否为6位数字
        if (!/^\d{6}$/.test(value)) {
            this.showError(input, '请输入6位数字验证码');
            return false;
        }
        this.clearError(input);
        return true;
    }

    // 显示错误信息
    showError(input, message) {
        const formItem = input.closest('.form-item');
        const errorElement = formItem.querySelector('.error-message');
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
        }
        input.style.borderColor = '#ff4757';
    }

    // 清除错误信息
    clearError(input) {
        const formItem = input.closest('.form-item');
        const errorElement = formItem.querySelector('.error-message');
        if (errorElement) {
            errorElement.classList.remove('show');
        }
        input.style.borderColor = '';
    }

    // 设置表单提交
    setupFormSubmit() {
        // 密码登录表单 - 改为表单提交
        const passwordForm = document.getElementById('password-form');
        if (passwordForm) {
            passwordForm.addEventListener('submit', (e) => {
                // 先进行客户端验证
                if (!this.validatePasswordLoginForm()) {
                    e.preventDefault();
                    return;
                }

                // 验证通过，让表单正常提交
                this.showLoading('password-submit');
                // 不需要preventDefault，让浏览器处理表单提交和重定向
            });
        }

        // 邮箱登录表单
        const emailForm = document.getElementById('email-form');
        if (emailForm) {
            emailForm.addEventListener('submit', (e) => {
                // 先进行客户端验证
                if (!this.validateEmailLoginForm()) {
                    e.preventDefault();
                    return;
                }

                // 验证通过，让表单正常提交
                this.showLoading('email-submit');
                // 不需要preventDefault，让浏览器处理表单提交和重定向
            });
        }
    }

    // 验证密码登录表单
    validatePasswordLoginForm() {
        const username = document.getElementById('username');
        const password = document.getElementById('password');
        const agreement = document.getElementById('agreement');

        // 验证表单
        if (!this.validateUsername(username)) return false;
        if (!this.validatePassword(password)) return false;
        if (!agreement.checked) {
            this.showToast('请同意用户协议、隐私政策和免责声明', 'warning');
            return false;
        }

        return true;
    }

    // 验证邮箱登录表单
    validateEmailLoginForm() {
        const emailInput = document.getElementById('identification');
        const verifyCodeInput = document.getElementById('verifyCode');
        const agreement = document.getElementById('email-agreement');

        // 验证表单
        if (!this.validateEmail(emailInput)) return false;
        if (!this.validateVerificationCode(verifyCodeInput)) return false;
        if (!agreement.checked) {
            this.showToast('请同意用户协议、隐私政策和免责声明', 'warning');
            return false;
        }

        return true;
    }

    // 处理密码登录
    handlePasswordLogin() {
        const username = document.getElementById('username');
        const password = document.getElementById('password');
        const agreement = document.getElementById('agreement');

        // 验证表单
        if (!this.validateUsername(username)) return;
        if (!this.validatePassword(password)) return;
        if (!agreement.checked) {
            this.showToast('请同意用户协议、隐私政策和免责声明', 'warning');
            return;
        }

        // 显示加载状态
        this.showLoading('password-submit');

        // 不再使用AJAX提交，改为表单提交
        // 将用户名和密码验证信息存储，让后端过滤器处理
        setTimeout(() => {
            // 表单提交成功后，浏览器会自动处理重定向
            // 这里我们只需要清理UI状态
            this.hideLoading('password-submit');
        }, 1000);
    }

    // 处理邮箱登录
    async handleEmailLogin() {
        const emailInput = document.getElementById('identification');
        const verifyCodeInput = document.getElementById('verifyCode');
        const agreement = document.getElementById('email-agreement');

        // 验证表单
        if (!this.validateEmail(emailInput)) return;
        if (!this.validateVerificationCode(verifyCodeInput)) return;
        if (!agreement.checked) {
            this.showToast('请同意用户协议、隐私政策和免责声明', 'warning');
            return;
        }

        // 显示加载状态
        this.showLoading('email-submit');

        try {
            const response = await fetch('/login/code', {
                method: 'POST',
                headers: this.addCsrfHeaders({
                    'Content-Type': 'application/json',
                }),
                body: JSON.stringify({
                    identification: emailInput.value.trim(),
                    verifyCode: verifyCodeInput.value.trim(),
                    usageType: "1" // CaptchaUsageType.LOGIN 字符串格式
                })
            });

            const result = await response.json();

            // code = 1 表示成功
            if (response.ok && result.code === 1) {
                this.hideLoading('email-submit');
                this.showToast('登录成功！', 'success');
                // 登录成功后跳转到主页或其他页面
                setTimeout(() => {
                    window.location.href = '/dashboard';
                }, 1500);
            } else {
                this.hideLoading('email-submit');
                this.showToast(result.message || '登录失败，请检查邮箱和验证码', 'error');
            }
        } catch (error) {
            this.hideLoading('email-submit');
            console.error('登录请求失败:', error);
            this.showToast('网络错误，请稍后重试', 'error');
        }
    }

    // 发送验证码
    async sendVerificationCode(btn) {
        // 直接获取邮箱输入框
        const emailInput = document.getElementById('identification');

        // 验证邮箱
        if (!this.validateEmail(emailInput)) {
            return;
        }

        try {
            const response = await fetch(`${GATEWAY_BASE_URL}/api/common/captcha/send`, {
                method: 'POST',
                headers: this.addCsrfHeaders({
                    'Content-Type': 'application/json',
                }),
                body: JSON.stringify({
                    identification: emailInput.value.trim(),
                    identificationType: 6, // UserAuthType.EMAIL
                    captchaUsageType: 1   // CaptchaUsageType.LOGIN
                })
            });

            const result = await response.json();

            // code = 1 表示成功
            if (response.ok && result.code === 1) {
                // 开始倒计时
                this.startCountdown(btn);
                // 发送验证码成功提示
                this.showToast('验证码已发送到您的邮箱，请查收！', 'success');
            } else {
                this.showToast(result.message || '发送验证码失败，请稍后重试', 'error');
            }
        } catch (error) {
            console.error('发送验证码请求失败:', error);
            this.showToast('网络错误，请稍后重试', 'error');
        }
    }

    // 开始倒计时
    startCountdown(btn) {
        btn.disabled = true;
        this.countdownTime = 60;

        const timer = setInterval(() => {
            if (this.countdownTime <= 0) {
                clearInterval(timer);
                btn.disabled = false;
                btn.textContent = '获取验证码';
                return;
            }

            btn.textContent = `${this.countdownTime}秒后重试`;
            this.countdownTime--;
        }, 1000);
    }

    // 处理社交登录
    handleSocialLogin(btn) {
        const platform = btn.dataset.social;
        alert(`正在使用${platform}登录...`);
        // 这里可以实现真实的社交登录逻辑
    }

    // 显示加载状态
    showLoading(submitId) {
        const btn = document.getElementById(submitId);
        if (btn) {
            btn.classList.add('loading');
            btn.disabled = true;
        }
    }

    // 隐藏加载状态
    hideLoading(submitId) {
        const btn = document.getElementById(submitId);
        if (btn) {
            btn.classList.remove('loading');
            btn.disabled = false;
        }
    }

    
    
    
    // 设置模态框
    setupModal() {
        // 初始化协议模态框
        this.termsLoader.initModal();

        // 协议链接点击事件
        document.querySelectorAll('.agreement-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const termsType = e.target.dataset.terms;
                this.termsLoader.showModal(termsType);
            });
        });
    }

    // 设置密码显示/隐藏功能
    setupPasswordToggle() {
        // 密码显示/隐藏按钮
        const passwordToggleBtn = document.getElementById('password-toggle');
        const passwordInput = document.getElementById('password');

        if (passwordToggleBtn && passwordInput) {
            passwordToggleBtn.addEventListener('click', () => {
                this.togglePasswordVisibility(passwordInput, passwordToggleBtn);
            });

            // 根据输入框内容动态显示/隐藏按钮
            passwordInput.addEventListener('input', () => {
                if (passwordInput.value.trim()) {
                    passwordToggleBtn.style.opacity = '1';
                    passwordToggleBtn.style.pointerEvents = 'auto';
                } else {
                    passwordToggleBtn.style.opacity = '0.3';
                    passwordToggleBtn.style.pointerEvents = 'none';
                }
            });

            // 初始化按钮状态
            if (passwordInput.value.trim()) {
                passwordToggleBtn.style.opacity = '1';
                passwordToggleBtn.style.pointerEvents = 'auto';
            } else {
                passwordToggleBtn.style.opacity = '0.3';
                passwordToggleBtn.style.pointerEvents = 'none';
            }
        }
    }

    // 切换密码显示/隐藏状态
    togglePasswordVisibility(passwordInput, toggleBtn) {
        const icon = toggleBtn.querySelector('.password-toggle-icon');

        if (passwordInput.type === 'password') {
            // 显示密码
            passwordInput.type = 'text';
            icon.classList.remove('far', 'fa-eye');
            icon.classList.add('far', 'fa-eye-slash');
            toggleBtn.title = '隐藏密码';
        } else {
            // 隐藏密码
            passwordInput.type = 'password';
            icon.classList.remove('far', 'fa-eye-slash');
            icon.classList.add('far', 'fa-eye');
            toggleBtn.title = '显示密码';
        }
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    new LoginAuth();
});