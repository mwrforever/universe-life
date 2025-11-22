// 万象生活注册页面逻辑
class RegisterAuth {
    constructor() {
        this.formValidator = null;
        this.init();
    }

    init() {
        // 初始化表单验证器
        this.formValidator = new FormValidator('register-form');

        // 初始化协议加载器
        this.termsLoader = new TermsLoader();

        // 清理所有表单内容
        this.clearAllForms();

        this.setupEventListeners();
        this.createFloatingIcons();
        this.setupFormValidation();
        this.setupModal();
  }

    // 清理所有表单内容
    clearAllForms() {
        // 清理注册表单
        const registerForm = document.getElementById('register-form');
        if (registerForm) {
            registerForm.reset();
            // 清除所有输入框的值
            const inputs = registerForm.querySelectorAll('input[type="email"], input[type="tel"], input[type="checkbox"]');
            inputs.forEach(input => {
                if (input.type === 'checkbox') {
                    input.checked = false;
                } else {
                    input.value = '';
                }
            });
            // 清除所有错误信息
            this.clearFormErrors(registerForm);
        }

        // 重置验证码按钮状态
        const sendCodeBtn = document.getElementById('send-code-btn');
        if (sendCodeBtn) {
            sendCodeBtn.disabled = false;
            sendCodeBtn.textContent = '获取验证码';
        }

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
        // 验证验证码并跳转到下一步
        const registerSubmit = document.getElementById('register-submit');
        if (registerSubmit) {
            registerSubmit.addEventListener('click', (e) => {
                e.preventDefault();
                this.handleRegister();
            });
        }

        // 绑定验证码发送按钮
        const sendCodeBtn = document.getElementById('send-code-btn');
        if (sendCodeBtn) {
            sendCodeBtn.addEventListener('click', () => {
                const identification = document.getElementById('identification').value;

                // 验证邮箱格式
                if (!identification || identification.trim() === '') {
                    this.formValidator.showFieldError('identification', '请输入邮箱地址');
                    return;
                }

                // 使用FormValidator验证邮箱格式
                const emailResult = this.formValidator.validateEmail(identification);
                if (!emailResult.isValid) {
                    this.formValidator.showFieldError('identification', emailResult.message);
                    return;
                }

                // 清除之前的错误
                this.formValidator.hideFieldError('identification');
                this.sendVerificationCode(identification, 2); // 2 = 注册验证码
            });
        }

        // 添加输入框清除错误的功能
        ['identification', 'verifyCode'].forEach(fieldId => {
            const input = document.getElementById(fieldId);
            if (input) {
                input.addEventListener('input', () => {
                    // 输入时清除该字段的错误提示
                    this.formValidator.hideFieldError(fieldId);
                });
            }
        });
    }

    // 设置表单验证
    setupFormValidation() {
        // FormValidator已经处理了验证逻辑
    }

    // 发送验证码函数
    sendVerificationCode(identification, usageType) {
        console.log('sendVerificationCode被调用:', identification, usageType);
        makeRequest('/common/captcha/send', {
            method: 'POST',
            body: {
                identification: identification,
                identificationType: 6,  // UserAuthType.EMAIL
                captchaUsageType: usageType  // CaptchaUsageType 数值
            },
            showSuccessToast: true,
            successMessage: '验证码发送成功'
        }).then(data => {
            console.log('验证码发送成功，开始倒计时:', data);
            // 开始倒计时
            this.startCountdown();
        }).catch(error => {
            console.log('验证码发送失败:', error);
            // 错误处理已在makeRequest中完成
            if (error.message && error.message.includes('identification')) {
                this.formValidator.showFieldError('identification', error.message);
            }
        });
    }

    // 验证验证码并跳转到下一步
    handleRegister() {
        const formData = this.formValidator.getFormData();

        // 客户端验证
        if (!this.formValidator.validateForm(formData, 'register')) {
            return;
        }

        // 发送请求
        makeRequest('/common/captcha/verify', {
            method: 'POST',
            body: {
                identification: formData.identification,
                verifyCode: formData.verifyCode,
                captchaUsageType: parseInt(formData.usageType)
            }
        }).then(data => {
            showToast('邮箱验证成功！请完善您的账户信息', 'success');

            // 构建跳转URL参数
            let url = `/register-info?email=${encodeURIComponent(formData.identification)}`;

            // 如果后端返回了issuer，添加到URL参数中
            if (data.data && data.data.issuer) {
                url += `&issuer=${encodeURIComponent(data.data.issuer)}`;
            }

            // 跳转到完善信息页面
            setTimeout(() => {
                window.location.href = url;
            }, 1500);
        }).catch(error => {
            this.formValidator.handleBackendErrors(error);

            // 处理具体的字段错误
            if (error.data && error.data.errors) {
                const errors = error.data.errors;
                if (errors.identification) {
                    this.formValidator.showFieldError('identification', errors.identification);
                }
                if (errors.verifyCode) {
                    this.formValidator.showFieldError('verifyCode', errors.verifyCode);
                }
            }
        });
    }

    // 倒计时函数
    startCountdown() {
        console.log('开始倒计时');
        let countdown = 60;
        const btn = document.getElementById('send-code-btn');

        if (btn) {
            console.log('找到按钮，开始倒计时');
            btn.disabled = true;
            const interval = setInterval(() => {
                countdown--;
                btn.textContent = `${countdown}秒后重试`;
                console.log('倒计时:', countdown);

                if (countdown <= 0) {
                    clearInterval(interval);
                    btn.disabled = false;
                    btn.textContent = '获取验证码';
                    console.log('倒计时结束');
                }
            }, 1000);
        } else {
            console.log('未找到倒计时按钮');
        }
    }
  // 设置表单验证
    setupFormValidation() {
        // FormValidator已经处理了验证逻辑
    }

    // 处理协议弹窗
    setupModal() {
        // 初始化协议模态框
        this.termsLoader.initModal();

        // 处理协议链接点击
        const agreementLinks = document.querySelectorAll('.agreement-link');
        agreementLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const termsType = link.getAttribute('data-terms');
                this.termsLoader.showModal(termsType);
            });
        });
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    new RegisterAuth();
});