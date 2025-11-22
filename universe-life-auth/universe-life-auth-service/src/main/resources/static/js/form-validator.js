/**
 * 统一的表单验证和错误处理工具类
 * 用于处理登录、注册等表单的验证和后端错误信息显示
 */
class FormValidator {
    constructor(formId) {
        this.form = document.getElementById(formId);
        this.errors = {};
        this.fieldErrorMap = {};

        if (this.form) {
            this.initErrorContainers();
        }
    }

    /**
     * 初始化错误容器映射
     */
    initErrorContainers() {
        this.fieldErrorMap = {
            // 通用字段
            'identification': 'identification-error',
            'verifyCode': 'verifyCode-error',
            'agreement': 'agreement-error',
            'email-agreement': 'email-agreement-error',

            // 密码登录字段
            'username': 'username-error',
            'password': 'password-error',

            // 注册信息字段
            'confirmPassword': 'confirmPassword-error'
        };
    }

    /**
     * 显示字段错误信息
     * @param {string} field 字段名
     * @param {string} message 错误信息
     */
    showFieldError(field, message) {
        const errorContainerId = this.fieldErrorMap[field];
        if (!errorContainerId) return;

        const errorContainer = document.getElementById(errorContainerId);
        if (errorContainer) {
            errorContainer.textContent = message;
            errorContainer.classList.add('show');
        }

        // 添加输入框错误样式
        const inputElement = document.getElementById(field);
        if (inputElement) {
            inputElement.classList.add('error');
        }
    }

    /**
     * 隐藏字段错误信息
     * @param {string} field 字段名
     */
    hideFieldError(field) {
        const errorContainerId = this.fieldErrorMap[field];
        if (!errorContainerId) return;

        const errorContainer = document.getElementById(errorContainerId);
        if (errorContainer) {
            errorContainer.textContent = '';
            errorContainer.classList.remove('show');
        }

        // 移除输入框错误样式
        const inputElement = document.getElementById(field);
        if (inputElement) {
            inputElement.classList.remove('error');
        }
    }

    /**
     * 清除所有错误信息
     */
    clearAllErrors() {
        Object.keys(this.fieldErrorMap).forEach(field => {
            this.hideFieldError(field);
        });
        this.errors = {};
    }

    /**
     * 处理后端返回的错误信息
     * @param {Object} errorData 后端错误数据
     */
    handleBackendErrors(errorData) {
        this.clearAllErrors();

        if (typeof errorData === 'string') {
            // 简单字符串错误，显示为toast
            showToast(errorData, 'error');
            return;
        }

        if (errorData.data && errorData.data.errors) {
            // 处理字段级错误
            const fieldErrors = errorData.data.errors;
            Object.keys(fieldErrors).forEach(field => {
                const errorMessage = Array.isArray(fieldErrors[field])
                    ? fieldErrors[field][0]
                    : fieldErrors[field];
                this.showFieldError(field, errorMessage);
            });
        }

        // 显示通用错误信息
        if (errorData.message) {
            showToast(errorData.message, 'error');
        }
    }

    /**
     * 验证邮箱格式
     * @param {string} email 邮箱地址
     * @returns {Object} {isValid: boolean, message: string}
     */
    validateEmail(email) {
        if (!email || email.trim() === '') {
            return { isValid: false, message: '请输入邮箱地址' };
        }

        // 更严格的邮箱格式验证
        const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

        if (!emailRegex.test(email)) {
            return { isValid: false, message: '邮箱账户格式错误，请输入正确的邮箱地址' };
        }

        // 检查邮箱长度
        if (email.length > 254) {
            return { isValid: false, message: '邮箱地址长度不能超过254个字符' };
        }

        // 检查是否包含中文字符
        if (/[\u4e00-\u9fa5]/.test(email)) {
            return { isValid: false, message: '邮箱地址不能包含中文字符' };
        }

        return { isValid: true, message: '' };
    }

    /**
     * 验证密码强度
     * @param {string} password 密码
     * @returns {Object} {isValid: boolean, message: string}
     */
    validatePassword(password) {
        if (!password || password.trim() === '') {
            return { isValid: false, message: '请输入密码' };
        }

        if (password.length < 6) {
            return { isValid: false, message: '密码长度不能少于6位' };
        }

        if (password.length > 20) {
            return { isValid: false, message: '密码长度不能超过20位' };
        }

        // 密码不能包含空格
        if (/\s/.test(password)) {
            return { isValid: false, message: '密码不能包含空格字符' };
        }

        // 建议密码包含字母和数字，但不强制
        return { isValid: true, message: '' };
    }

    /**
     * 验证验证码格式
     * @param {string} code 验证码
     * @returns {Object} {isValid: boolean, message: string}
     */
    validateVerifyCode(code) {
        if (!code || code.trim() === '') {
            return { isValid: false, message: '请输入验证码' };
        }

        if (!/^\d{6}$/.test(code)) {
            return { isValid: false, message: '验证码必须是6位数字' };
        }

        return { isValid: true, message: '' };
    }

    /**
     * 验证用户名格式
     * @param {string} username 用户名
     * @returns {Object} {isValid: boolean, message: string}
     */
    validateUsername(username) {
        if (!username || username.trim() === '') {
            return { isValid: false, message: '请输入用户名' };
        }

        if (username.length < 4) {
            return { isValid: false, message: '用户名长度不能少于4位' };
        }

        if (username.length > 16) {
            return { isValid: false, message: '用户名长度不能超过16位' };
        }

        // 用户名只能包含字母、数字、下划线和连字符（与后端验证规则一致）
        const usernameRegex = /^[a-zA-Z0-9_-]+$/;
        if (!usernameRegex.test(username)) {
            return { isValid: false, message: '用户名只能包含字母、数字、下划线和连字符' };
        }

        return { isValid: true, message: '' };
    }

    /**
     * 客户端表单验证
     * @param {Object} formData 表单数据
     * @param {string} formType 表单类型 ('login-password', 'login-code', 'register', 'register-info')
     * @returns {boolean}
     */
    validateForm(formData, formType) {
        this.clearAllErrors();
        let isValid = true;

        switch (formType) {
            case 'login-password':
                // 密码登录验证
                if (!formData.username || formData.username.trim() === '') {
                    this.showFieldError('username', '请输入用户名');
                    isValid = false;
                }

                if (!formData.password || formData.password.trim() === '') {
                    this.showFieldError('password', '请输入密码');
                    isValid = false;
                } else {
                    // 验证密码格式
                    const passwordResult = this.validatePassword(formData.password);
                    if (!passwordResult.isValid) {
                        this.showFieldError('password', passwordResult.message);
                        isValid = false;
                    }
                }

                // 验证协议勾选
                if (!formData.agreement) {
                    this.showFieldError('agreement', '请阅读并同意用户服务协议、隐私政策和平台免责声明');
                    isValid = false;
                }
                break;

            case 'login-code':
                // 验证码登录验证
                if (!formData.identification || formData.identification.trim() === '') {
                    this.showFieldError('identification', '请输入邮箱地址');
                    isValid = false;
                } else {
                    // 验证邮箱格式
                    const emailResult = this.validateEmail(formData.identification);
                    if (!emailResult.isValid) {
                        this.showFieldError('identification', emailResult.message);
                        isValid = false;
                    }
                }

                if (!formData.verifyCode || formData.verifyCode.trim() === '') {
                    this.showFieldError('verifyCode', '请输入验证码');
                    isValid = false;
                } else {
                    // 验证验证码格式
                    const codeResult = this.validateVerifyCode(formData.verifyCode);
                    if (!codeResult.isValid) {
                        this.showFieldError('verifyCode', codeResult.message);
                        isValid = false;
                    }
                }

                // 验证协议勾选
                if (!formData['email-agreement']) {
                    this.showFieldError('email-agreement', '请阅读并同意用户服务协议、隐私政策和平台免责声明');
                    isValid = false;
                }
                break;

            case 'register':
                // 注册验证
                if (!formData.identification || formData.identification.trim() === '') {
                    this.showFieldError('identification', '请输入邮箱地址');
                    isValid = false;
                } else {
                    // 验证邮箱格式
                    const emailResult = this.validateEmail(formData.identification);
                    if (!emailResult.isValid) {
                        this.showFieldError('identification', emailResult.message);
                        isValid = false;
                    }
                }

                if (!formData.verifyCode || formData.verifyCode.trim() === '') {
                    this.showFieldError('verifyCode', '请输入验证码');
                    isValid = false;
                } else {
                    // 验证验证码格式
                    const codeResult = this.validateVerifyCode(formData.verifyCode);
                    if (!codeResult.isValid) {
                        this.showFieldError('verifyCode', codeResult.message);
                        isValid = false;
                    }
                }

                // 验证协议勾选
                if (!formData.agreement) {
                    this.showFieldError('agreement', '请阅读并同意用户服务协议、隐私政策和平台免责声明');
                    isValid = false;
                }
                break;

            case 'register-info':
                // 注册信息验证
                if (!formData.username || formData.username.trim() === '') {
                    this.showFieldError('username', '请输入用户名');
                    isValid = false;
                } else {
                    // 验证用户名格式
                    const usernameResult = this.validateUsername(formData.username);
                    if (!usernameResult.isValid) {
                        this.showFieldError('username', usernameResult.message);
                        isValid = false;
                    }
                }

                if (!formData.password || formData.password.trim() === '') {
                    this.showFieldError('password', '请输入密码');
                    isValid = false;
                } else {
                    // 验证密码格式
                    const passwordResult = this.validatePassword(formData.password);
                    if (!passwordResult.isValid) {
                        this.showFieldError('password', passwordResult.message);
                        isValid = false;
                    }
                }

                if (!formData.confirmPassword || formData.confirmPassword.trim() === '') {
                    this.showFieldError('confirmPassword', '请再次输入密码');
                    isValid = false;
                } else if (formData.password !== formData.confirmPassword) {
                    this.showFieldError('confirmPassword', '两次输入的密码不一致，请重新输入');
                    isValid = false;
                }
                break;
        }

        return isValid;
    }

    /**
     * 获取表单数据
     * @returns {Object}
     */
    getFormData() {
        const formData = {};
        const inputs = this.form.querySelectorAll('input, select, textarea');

        inputs.forEach(input => {
            const name = input.name;
            const value = input.value;

            if (input.type === 'checkbox') {
                formData[name] = input.checked;
            } else {
                formData[name] = value;
            }
        });

        return formData;
    }
}

/**
 * Toast 提示函数
 * @param {string} message 提示信息
 * @param {string} type 类型 ('success', 'error', 'warning', 'info')
 */
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type} show`;

    let icon = '';
    switch (type) {
        case 'success':
            icon = '✓';
            break;
        case 'error':
            icon = '✗';
            break;
        case 'warning':
            icon = '⚠';
            break;
        case 'info':
        default:
            icon = 'ℹ';
            break;
    }

    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-message">${message}</div>
        <button class="toast-close" onclick="this.parentElement.remove()">×</button>
    `;

    const container = document.getElementById('toast-container');
    if (!container) {
        console.warn('Toast container not found');
        return;
    }

    container.appendChild(toast);

    // 自动隐藏
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentElement) {
                toast.parentElement.removeChild(toast);
            }
        }, 300);
    }, 4000);
}

/**
 * 处理API响应
 * @param {Response} response fetch响应对象
 * @param {Object} options 选项 {showSuccessToast: boolean, successMessage: string}
 * @returns {Promise}
 */
function handleApiResponse(response, options = {}) {
    const { showSuccessToast = false, successMessage = '操作成功' } = options;

    return response.json().then(data => {
        if (data.code === 0) {
            if (showSuccessToast) {
                showToast(successMessage || data.message || '操作成功', 'success');
            }
            return { success: true, data };
        } else {
            // 业务错误
            return { success: false, data };
        }
    }).catch(error => {
        // JSON解析错误或网络错误
        console.error('API响应处理错误:', error);
        return { success: false, error: error.message };
    });
}

/**
 * 通用的API请求函数
 * @param {string} url 请求URL
 * @param {Object} options 请求选项
 * @returns {Promise}
 */
async function makeRequest(url, options = {}) {
    const {
        method = 'GET',
        body = null,
        headers = {},
        showLoading = true,
        showSuccessToast = false,
        successMessage = '操作成功'
    } = options;

    console.log('makeRequest调用:', url, method, body);

    // 获取CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const defaultHeaders = {
        'Content-Type': 'application/json',
        ...(csrfToken && { [csrfHeader]: csrfToken }),
        ...headers
    };

    console.log('请求头:', defaultHeaders);

    try {
        const response = await fetch(url, {
            method,
            headers: defaultHeaders,
            body: body ? JSON.stringify(body) : null
        });

        console.log('响应状态:', response.status, response.ok);

        const result = await handleApiResponse(response, { showSuccessToast, successMessage });
        console.log('处理结果:', result);

        if (result.success) {
            return result.data;
        } else {
            throw result.data;
        }
    } catch (error) {
        console.error('请求失败:', error);
        throw error;
    }
}

// 导出到全局
window.FormValidator = FormValidator;
window.showToast = showToast;
window.makeRequest = makeRequest;
window.handleApiResponse = handleApiResponse;