/**
 * 协议加载器
 * 负责从后端API获取协议内容并动态显示在模态框中
 */
class TermsLoader {
    constructor() {
        this.termsCache = {
            user: null,
            privacy: null,
            disclaimer: null
        };
        this.loading = {
            user: false,
            privacy: false,
            disclaimer: false
        };
    }

    /**
     * 获取用户服务协议
     * @returns {Promise} 用户服务协议内容Promise
     */
    async getUserAgreement() {
        if (this.termsCache.user) {
            return this.termsCache.user;
        }

        if (this.loading.user) {
            return new Promise((resolve) => {
                const checkCache = () => {
                    if (this.termsCache.user) {
                        resolve(this.termsCache.user);
                    } else {
                        setTimeout(checkCache, 100);
                    }
                };
                checkCache();
            });
        }

        this.loading.user = true;

        try {
            const response = await makeRequest('/user-agreement', {
                method: 'GET',
                showLoading: false
            });

            this.termsCache.user = response.data.content;
            return this.termsCache.user;
        } catch (error) {
            throw error;
        } finally {
            this.loading.user = false;
        }
    }

    /**
     * 获取隐私政策
     * @returns {Promise} 隐私政策内容Promise
     */
    async getPrivacyPolicy() {
        if (this.termsCache.privacy) {
            return this.termsCache.privacy;
        }

        if (this.loading.privacy) {
            return new Promise((resolve) => {
                const checkCache = () => {
                    if (this.termsCache.privacy) {
                        resolve(this.termsCache.privacy);
                    } else {
                        setTimeout(checkCache, 100);
                    }
                };
                checkCache();
            });
        }

        this.loading.privacy = true;

        try {
            const response = await makeRequest('/privacy-policy', {
                method: 'GET',
                showLoading: false
            });

            this.termsCache.privacy = response.data.content;
            return this.termsCache.privacy;
        } catch (error) {
            throw error;
        } finally {
            this.loading.privacy = false;
        }
    }

    /**
     * 获取平台免责声明
     * @returns {Promise} 平台免责声明内容Promise
     */
    async getDisclaimer() {
        if (this.termsCache.disclaimer) {
            return this.termsCache.disclaimer;
        }

        if (this.loading.disclaimer) {
            return new Promise((resolve) => {
                const checkCache = () => {
                    if (this.termsCache.disclaimer) {
                        resolve(this.termsCache.disclaimer);
                    } else {
                        setTimeout(checkCache, 100);
                    }
                };
                checkCache();
            });
        }

        this.loading.disclaimer = true;

        try {
            const response = await makeRequest('/disclaimer', {
                method: 'GET',
                showLoading: false
            });

            this.termsCache.disclaimer = response.data.content;
            return this.termsCache.disclaimer;
        } catch (error) {
            throw error;
        } finally {
            this.loading.disclaimer = false;
        }
    }

    /**
     * 显示协议内容到指定容器
     * @param {string} termsType 协议类型 ('user', 'privacy', 'disclaimer')
     * @param {HTMLElement} container 容器元素
     */
    async displayTerms(termsType, container) {
        try {
            let content = '';

            switch (termsType) {
                case 'user':
                    content = await this.getUserAgreement();
                    break;
                case 'privacy':
                    content = await this.getPrivacyPolicy();
                    break;
                case 'disclaimer':
                    content = await this.getDisclaimer();
                    break;
                default:
                    content = '<p>未知协议类型</p>';
            }

            if (!content) {
                content = '<p>协议内容加载失败</p>';
            }

            container.innerHTML = content;
        } catch (error) {
            container.innerHTML = '<p>协议内容加载失败，请稍后重试</p>';
        }
    }

    /**
     * 初始化协议模态框
     * @param {Object} options 配置选项
     */
    initModal(options = {}) {
        const {
            modalId = 'terms-modal',
            titleId = 'modal-title',
            tabButtonClass = 'modal-tab',
            contentClass = 'terms-content',
            closeButtonId = 'modal-close',
            confirmButtonId = 'modal-confirm'
        } = options;

        const modal = document.getElementById(modalId);
        const modalTitle = document.getElementById(titleId);
        const tabButtons = modal?.querySelectorAll(`.${tabButtonClass}`);
        const contents = modal?.querySelectorAll(`.${contentClass}`);

        if (!modal || !modalTitle || !tabButtons?.length || !contents?.length) {
            return;
        }

        // 设置标题映射
        const titles = {
            user: '用户服务协议',
            privacy: '隐私政策',
            disclaimer: '平台免责声明'
        };

        // 处理标签切换
        tabButtons.forEach(tab => {
            tab.addEventListener('click', async (e) => {
                const targetTab = e.target.dataset.tab;
                if (!targetTab) return;

                // 更新标题
                modalTitle.textContent = titles[targetTab] || '用户协议';

                // 更新标签状态
                tabButtons.forEach(btn => btn.classList.remove('active'));
                e.target.classList.add('active');

                // 更新内容
                contents.forEach(content => content.classList.remove('active'));

                const targetContent = modal.querySelector(`#${targetTab}-content`);
                if (targetContent) {
                    targetContent.classList.add('active');
                    await this.displayTerms(targetTab, targetContent);
                }
            });
        });

        // 处理关闭按钮
        const closeButton = document.getElementById(closeButtonId);
        const confirmButton = document.getElementById(confirmButtonId);

        if (closeButton) {
            closeButton.addEventListener('click', () => {
                modal.classList.remove('show');
            });
        }

        if (confirmButton) {
            confirmButton.addEventListener('click', () => {
                modal.classList.remove('show');
            });
        }

        // 点击背景关闭
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('show');
            }
        });
    }

    /**
     * 显示协议模态框
     * @param {string} termsType 默认显示的协议类型
     * @param {Object} options 配置选项
     */
    async showModal(termsType = 'user', options = {}) {
        const { modalId = 'terms-modal' } = options;
        const modal = document.getElementById(modalId);

        if (!modal) {
            return;
        }

        // 先显示模态框
        modal.classList.add('show');

        // 激活对应的标签
        const targetTab = modal.querySelector(`[data-tab="${termsType}"]`);
        if (targetTab) {
            targetTab.click();
        }
    }
}

// 导出到全局
window.TermsLoader = TermsLoader;