// 万象生活整合界面逻辑
class UnifiedAuth {
    constructor() {
        this.currentSection = 'login';
        this.currentLoginTab = 'password';
        this.countdownTimer = null;
        this.countdownTime = 60;
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupFormValidation();
        this.setupModal();
        this.createFloatingIcons();
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
        // 主导航标签切换
        document.querySelectorAll('.nav-tab').forEach(tab => {
            tab.addEventListener('click', (e) => {
                const targetSection = e.target.dataset.section;
                this.switchSection(targetSection);
            });
        });

        // 登录标签切换
        document.querySelectorAll('.tab-button').forEach(tab => {
            tab.addEventListener('click', (e) => {
                const targetTab = e.target.dataset.tab;
                this.switchLoginTab(targetTab);
            });
        });

        // 返回按钮
        const backBtn = document.getElementById('back-link');
        if (backBtn) {
            backBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchSection('register');
            });
        }

        // 表单提交事件
        this.setupFormSubmit();

        // 验证码按钮
        document.querySelectorAll('.verification-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.sendVerificationCode(e.target);
            });
        });

        // 社交登录按钮
        document.querySelectorAll('.social-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.handleSocialLogin(e.target);
            });
        });
    }

    // 切换主要部分
    switchSection(section) {
        // 更新导航标签状态
        document.querySelectorAll('.nav-tab').forEach(tab => {
            tab.classList.remove('active');
            if (tab.dataset.section === section) {
                tab.classList.add('active');
            }
        });

        // 切换内容区域
        document.querySelectorAll('.form-section').forEach(section => {
            section.classList.remove('active');
        });

        document.getElementById(`${section}-section`).classList.add('active');

        this.currentSection = section;

        // 更新底部链接文字
        this.updateFooterLinks(section);

        // 特殊处理
        if (section === 'login') {
            // 重置登录标签
            if (this.currentLoginTab) {
                this.switchLoginTab(this.currentLoginTab);
            }
        }
    }

    // 切换登录标签页
    switchLoginTab(tab) {
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

    // 设置表单验证
    setupFormValidation() {
        // 用户名验证
        const usernameInputs = document.querySelectorAll('#username, #reg-username');
        usernameInputs.forEach(input => {
            input.addEventListener('blur', () => this.validateUsername(input));
            input.addEventListener('input', () => this.clearError(input));
        });

        // 邮箱验证
        const emailInputs = document.querySelectorAll('#email, #reg-email');
        emailInputs.forEach(input => {
            input.addEventListener('blur', () => this.validateEmail(input));
            input.addEventListener('input', () => this.clearError(input));
        });

        // 密码验证
        const passwordInputs = document.querySelectorAll('#password, #reg-password');
        passwordInputs.forEach(input => {
            input.addEventListener('blur', () => this.validatePassword(input));
            input.addEventListener('input', () => this.clearError(input));
        });

        // 确认密码验证
        const confirmPasswordInput = document.getElementById('confirmPassword');
        if (confirmPasswordInput) {
            confirmPasswordInput.addEventListener('blur', () => this.validateConfirmPassword());
            confirmPasswordInput.addEventListener('input', () => this.clearError(confirmPasswordInput));
        }

        // 昵称验证
        const nicknameInput = document.getElementById('nickname');
        if (nicknameInput) {
            nicknameInput.addEventListener('blur', () => this.validateNickname(nicknameInput));
            nicknameInput.addEventListener('input', () => this.clearError(nicknameInput));
        }
    }

    // 验证用户名
    validateUsername(input) {
        const value = input.value.trim();
        if (!value) {
            this.showError(input, '请输入用户名');
            return false;
        }
        if (value.length < 3 || value.length > 20) {
            this.showError(input, '用户名长度应在3-20个字符之间');
            return false;
        }
        if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(value)) {
            this.showError(input, '用户名只能包含中文、英文、数字、下划线');
            return false;
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
        if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
            this.showError(input, '密码必须包含字母和数字');
            return false;
        }
        this.clearError(input);
        return true;
    }

    // 验证确认密码
    validateConfirmPassword() {
        const password = document.getElementById('reg-password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (!confirmPassword) {
            this.showError(document.getElementById('confirmPassword'), '请确认密码');
            return false;
        }

        if (password !== confirmPassword) {
            this.showError(document.getElementById('confirmPassword'), '两次输入的密码不一致');
            return false;
        }

        this.clearError(document.getElementById('confirmPassword'));
        return true;
    }

    // 验证昵称
    validateNickname(input) {
        const value = input.value.trim();
        if (!value) {
            this.showError(input, '请输入昵称');
            return false;
        }
        if (value.length < 2 || value.length > 20) {
            this.showError(input, '昵称长度应在2-20个字符之间');
            return false;
        }
        if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
            this.showError(input, '昵称只能包含中文、英文、数字');
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
        // 密码登录表单
        const passwordForm = document.getElementById('password-form');
        if (passwordForm) {
            passwordForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handlePasswordLogin();
            });
        }

        // 邮箱登录表单
        const emailForm = document.getElementById('email-form');
        if (emailForm) {
            emailForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleEmailLogin();
            });
        }

        // 注册表单
        const registerForm = document.getElementById('register-form');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleRegister();
            });
        }

        // 完善信息表单
        const registerInfoForm = document.getElementById('register-info-form');
        if (registerInfoForm) {
            registerInfoForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleRegisterInfo();
            });
        }
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
            alert('请同意用户协议、隐私政策和免责声明');
            return;
        }

        // 显示加载状态
        this.showLoading('password-submit');

        // 模拟登录请求
        setTimeout(() => {
            this.hideLoading('password-submit');
            alert('登录成功！');
            // 这里可以跳转到主页
            // window.location.href = '/dashboard';
        }, 2000);
    }

    // 处理邮箱登录
    handleEmailLogin() {
        const email = document.getElementById('email');
        const verificationCode = document.getElementById('email-verification-code');
        const agreement = document.getElementById('email-agreement');

        // 验证表单
        if (!this.validateEmail(email)) return;
        if (!verificationCode.value.trim()) {
            this.showError(verificationCode, '请输入验证码');
            return;
        }
        if (!agreement.checked) {
            alert('请同意用户协议、隐私政策和免责声明');
            return;
        }

        // 显示加载状态
        this.showLoading('email-submit');

        // 模拟登录请求
        setTimeout(() => {
            this.hideLoading('email-submit');
            alert('登录成功！');
            // 这里可以跳转到主页
            // window.location.href = '/dashboard';
        }, 2000);
    }

    // 处理注册
    handleRegister() {
        const email = document.getElementById('reg-email');
        const verificationCode = document.getElementById('reg-verification-code');
        const agreement = document.getElementById('reg-agreement');

        // 验证表单
        if (!this.validateEmail(email)) return;
        if (!verificationCode.value.trim()) {
            this.showError(verificationCode, '请输入验证码');
            return;
        }
        if (!agreement.checked) {
            alert('请同意用户协议、隐私政策和免责声明');
            return;
        }

        // 显示加载状态
        this.showLoading('register-submit');

        // 模拟验证请求
        setTimeout(() => {
            this.hideLoading('register-submit');

            // 保存注册邮箱信息
            this.saveRegisterData({
                email: email.value.trim(),
                verified: true
            });

            // 跳转到完善信息页面
            this.switchSection('complete-info');

            // 显示已验证的邮箱
            const emailDisplay = document.getElementById('phone-display');
            if (emailDisplay) {
                emailDisplay.textContent = this.maskEmail(email.value.trim());
            }
        }, 2000);
    }

    // 处理完善注册信息
    handleRegisterInfo() {
        const username = document.getElementById('reg-username');
        const nickname = document.getElementById('nickname');
        const password = document.getElementById('reg-password');
        const confirmPassword = document.getElementById('confirmPassword');

        // 验证表单
        if (!this.validateUsername(username)) return;
        if (!this.validateNickname(nickname)) return;
        if (!this.validatePassword(password)) return;
        if (!this.validateConfirmPassword()) return;

        // 显示加载状态
        this.showLoading('register-info-submit');

        // 模拟注册请求
        setTimeout(() => {
            this.hideLoading('register-info-submit');
            alert('注册成功！');

            // 跳转到登录页面
            this.switchSection('login');
        }, 2000);
    }

    // 邮箱掩码
    maskEmail(email) {
        const [localPart, domain] = email.split('@');
        if (localPart.length <= 3) {
            return `${localPart[0]}***@${domain}`;
        }
        return `${localPart.substring(0, 3)}***@${domain}`;
    }

    // 保存注册数据
    saveRegisterData(data) {
        // 这里可以使用sessionStorage或localStorage
        sessionStorage.setItem('registerData', JSON.stringify(data));
    }

    // 获取注册数据
    getRegisterData() {
        const data = sessionStorage.getItem('registerData');
        return data ? JSON.parse(data) : null;
    }

    // 发送验证码
    sendVerificationCode(btn) {
        const emailInput = btn.closest('.form-item').querySelector('input[type="email"], input[type="text"]');

        // 验证邮箱
        if (!this.validateEmail(emailInput)) {
            return;
        }

        // 开始倒计时
        this.startCountdown(btn);

        // 模拟发送验证码
        alert('验证码已发送到您的邮箱，请查收！');
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
        const modal = document.getElementById('terms-modal');
        const modalClose = document.getElementById('modal-close');
        const modalConfirm = document.getElementById('modal-confirm');

        // 协议链接点击事件
        document.querySelectorAll('.agreement-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const termsType = e.target.dataset.terms;
                this.showModal(termsType);
            });
        });

        // 关闭模态框
        if (modalClose) {
            modalClose.addEventListener('click', () => this.hideModal());
        }

        if (modalConfirm) {
            modalConfirm.addEventListener('click', () => this.hideModal());
        }

        // 点击背景关闭
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.hideModal();
                }
            });
        }

        // 模态框标签切换
        document.querySelectorAll('.modal-tab').forEach(tab => {
            tab.addEventListener('click', (e) => {
                const targetTab = e.target.dataset.tab;
                this.switchModalTab(targetTab);
            });
        });
    }

    // 显示模态框
    showModal(termsType) {
        const modal = document.getElementById('terms-modal');
        const modalTitle = document.getElementById('modal-title');

        modal.classList.add('show');

        // 设置标题
        const titles = {
            user: '用户服务协议',
            privacy: '隐私政策',
            disclaimer: '平台免责声明'
        };

        if (modalTitle) {
            modalTitle.textContent = titles[termsType] || '用户服务协议';
        }

        // 切换到对应的标签
        this.switchModalTab(termsType);
    }

    // 隐藏模态框
    hideModal() {
        const modal = document.getElementById('terms-modal');
        modal.classList.remove('show');
    }

    // 切换模态框标签
    switchModalTab(tab) {
        // 更新标签按钮状态
        document.querySelectorAll('.modal-tab').forEach(btn => {
            btn.classList.remove('active');
            if (btn.dataset.tab === tab) {
                btn.classList.add('active');
            }
        });

        // 切换内容
        document.querySelectorAll('.terms-content').forEach(content => {
            content.classList.remove('active');
        });

        const targetContent = document.getElementById(`${tab}-content`);
        if (targetContent) {
            targetContent.classList.add('active');
        }
    }

    // 更新底部链接文字
    updateFooterLinks(section) {
        const linkText = document.getElementById('auth-link-text');
        const linkAction = document.getElementById('auth-link-action');

        if (!linkText || !linkAction) return;

        const linkConfigs = {
            login: {
                text: '还没有账号？',
                linkText: '立即注册',
                targetSection: 'register'
            },
            register: {
                text: '已有账号？',
                linkText: '立即登录',
                targetSection: 'login'
            },
            'complete-info': {
                text: '已有账号？',
                linkText: '立即登录',
                targetSection: 'login'
            }
        };

        const config = linkConfigs[section] || linkConfigs.login;

        linkText.textContent = config.text;
        linkAction.textContent = config.linkText;
        linkAction.onclick = () => {
            this.switchSection(config.targetSection);
        };
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    new UnifiedAuth();
});