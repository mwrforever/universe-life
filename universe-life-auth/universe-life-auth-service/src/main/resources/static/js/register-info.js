// 万象生活完善信息页面逻辑
class RegisterInfoAuth {
    constructor() {
        this.formValidator = null;
        this.init();
    }

    init() {
        // 初始化表单验证器
        this.formValidator = new FormValidator('register-info-form');

        // 清理所有表单内容（除了通过URL传递的数据）
        this.clearAllForms();

        this.setupEventListeners();
        this.loadRegisterData();
        this.createFloatingIcons();
        this.setupFormValidation();
        this.setupPasswordToggle();
    }

    // 清理所有表单内容
    clearAllForms() {
        // 清理完善信息表单（只清理用户输入的字段，保留hidden字段）
        const registerInfoForm = document.getElementById('register-info-form');
        if (registerInfoForm) {
            // 清除所有用户可编辑的输入框
            const userInputs = registerInfoForm.querySelectorAll('input[type="text"], input[type="password"]');
            userInputs.forEach(input => {
                input.value = '';
            });
            // 清除所有错误信息
            this.clearFormErrors(registerInfoForm);
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

    // 加载注册数据
    loadRegisterData() {
        // 从URL参数获取邮箱和issuer
        const urlParams = new URLSearchParams(window.location.search);
        const email = urlParams.get('email');
        const issuer = urlParams.get('issuer');

        if (email) {
            const identificationInput = document.getElementById('identification');
            if (identificationInput) {
                identificationInput.value = email;
            }
        }

        if (issuer) {
            const issuerInput = document.getElementById('issuer');
            if (issuerInput) {
                issuerInput.value = issuer;
            }
        }
    }

    // 设置事件监听器
    setupEventListeners() {
        // 返回按钮使用直接链接，无需额外事件处理

        // 表单提交事件
        const registerInfoForm = document.getElementById('register-info-form');
        if (registerInfoForm) {
            registerInfoForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleRegisterInfo();
            });
        }
    }

    // 设置表单验证
    setupFormValidation() {
        // 添加输入框清除错误的功能
        ['username', 'password', 'confirmPassword'].forEach(fieldId => {
            const input = document.getElementById(fieldId);
            if (input) {
                input.addEventListener('input', () => {
                    this.formValidator.hideFieldError(fieldId);
                });
            }
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
        }

        // 确认密码显示/隐藏按钮
        const confirmPasswordToggleBtn = document.getElementById('confirm-password-toggle');
        const confirmPasswordInput = document.getElementById('confirmPassword');

        if (confirmPasswordToggleBtn && confirmPasswordInput) {
            confirmPasswordToggleBtn.addEventListener('click', () => {
                this.togglePasswordVisibility(confirmPasswordInput, confirmPasswordToggleBtn);
            });

            // 根据输入框内容动态显示/隐藏按钮
            confirmPasswordInput.addEventListener('input', () => {
                if (confirmPasswordInput.value.trim()) {
                    confirmPasswordToggleBtn.style.opacity = '1';
                    confirmPasswordToggleBtn.style.pointerEvents = 'auto';
                } else {
                    confirmPasswordToggleBtn.style.opacity = '0.3';
                    confirmPasswordToggleBtn.style.pointerEvents = 'none';
                }
            });
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

    // 处理完善注册信息
    handleRegisterInfo() {
        const formData = this.formValidator.getFormData();

        // 客户端验证
        if (!this.formValidator.validateForm(formData, 'register-info')) {
            return;
        }

        // 显示加载状态
        this.showLoading('register-submit');

        // 验证必填字段
        if (!formData.issuer || formData.issuer.trim() === '') {
            showToast('验证码请求标识不能为空', 'error');
            return;
        }

        // 发送注册请求
        const requestBody = {
            identification: formData.identification,
            identificationType: 6, // UserAuthType.EMAIL
            captchaUsageType: 2,   // CaptchaUsageType.REGISTER
            username: formData.username,
            password: formData.password,
            issuer: formData.issuer.trim() // 验证码请求唯一标识（必填）
        };

        console.log('注册请求数据:', requestBody);
        console.log('发送请求到: /register');

        makeRequest('/register', {
            method: 'POST',
            body: requestBody
        }).then(data => {
            this.hideLoading('register-submit');
            showToast('注册成功！请使用新账号登录', 'success');

            // 跳转到登录页面
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        }).catch(error => {
            this.hideLoading('register-submit');
            console.error('注册请求失败:', error);

            // 显示详细的错误信息
            if (typeof error === 'string') {
                showToast(error, 'error');
            } else if (error.message) {
                showToast(error.message, 'error');
            } else if (error.error) {
                showToast(error.error, 'error');
            } else {
                showToast('注册失败，请检查输入信息', 'error');
            }

            this.formValidator.handleBackendErrors(error);

            // 处理具体的字段错误
            if (error.data && error.data.errors) {
                const errors = error.data.errors;
                if (errors.username) {
                    this.formValidator.showFieldError('username', errors.username);
                }
                if (errors.password) {
                    this.formValidator.showFieldError('password', errors.password);
                }
                if (errors.confirmPassword) {
                    this.formValidator.showFieldError('confirmPassword', errors.confirmPassword);
                }
            }
        });
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
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    new RegisterInfoAuth();
});