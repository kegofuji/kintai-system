// 認証サービス（設計書のAPI仕様完全準拠）
class AuthService {
    constructor() {
        this.baseUrl = CONFIG.getApiBaseUrl();
        this.sessionKey = 'kintai_session_token';
    }

    /**
     * 共通API呼び出し処理
     * @param {string} endpoint - エンドポイント
     * @param {Object} options - オプション
     * @returns {Promise<Object>} API応答
     */
    async apiCall(endpoint, options = {}) {
        try {
            const url = `${this.baseUrl}${endpoint}`;
            const token = this.getSessionToken();
            
            const defaultOptions = {
                headers: {
                    'Content-Type': 'application/json',
                    ...(token && { 'Authorization': `Bearer ${token}` })
                }
            };

            const response = await fetch(url, {
                ...defaultOptions,
                ...options,
                headers: {
                    ...defaultOptions.headers,
                    ...options.headers
                }
            });

            const data = await response.json();

            if (response.ok) {
                return {
                    success: true,
                    data: data,
                    message: data.message || 'Success'
                };
            } else {
                return {
                    success: false,
                    message: data.message || `HTTP Error: ${response.status}`,
                    error: data.error || 'Unknown error'
                };
            }
        } catch (error) {
            console.error('API call error:', error);
            return {
                success: false,
                message: 'ネットワークエラーが発生しました',
                error: error.message
            };
        }
    }

    /**
     * ログイン処理（POST /api/auth/login）
     * 設計書API仕様準拠
     * @param {string} employeeCode - 社員コード
     * @param {string} password - パスワード
     * @returns {Promise<Object>} ログイン結果
     */
    async login(employeeCode, password) {
        const response = await this.apiCall('/auth/login', {
            method: 'POST',
            body: JSON.stringify({
                employeeCode: employeeCode,
                password: password
            })
        });
        
        if (response.success) {
            this.setSessionToken(response.data.sessionToken);
            // ユーザー情報も保存
            if (response.data.user) {
                localStorage.setItem(CONFIG.STORAGE_KEYS.USER_INFO, JSON.stringify(response.data.user));
            }
        }
        
        return response;
    }

    /**
     * ログアウト処理（POST /api/auth/logout）
     * @returns {Promise<Object>} ログアウト結果
     */
    async logout() {
        const response = await this.apiCall('/auth/logout', {
            method: 'POST'
        });
        
        this.clearSessionToken();
        // ユーザー情報もクリア
        localStorage.removeItem(CONFIG.STORAGE_KEYS.USER_INFO);
        
        return response;
    }

    /**
     * セッション確認（GET /api/auth/session）
     * @returns {Promise<Object>} セッション状態
     */
    async checkSession() {
        return await this.apiCall('/auth/session', {
            method: 'GET'
        });
    }

    /**
     * 認証状態確認（checkSessionのエイリアス）
     * @returns {Promise<Object>} 認証状態
     */
    async checkAuth() {
        const response = await this.checkSession();
        
        if (response.success) {
            return {
                authenticated: true,
                user: response.data.user
            };
        } else {
            return {
                authenticated: false
            };
        }
    }

    /**
     * セッショントークン管理
     * @returns {string|null} セッショントークン
     */
    getSessionToken() {
        return localStorage.getItem(this.sessionKey);
    }

    /**
     * セッショントークン設定
     * @param {string} token - セッショントークン
     */
    setSessionToken(token) {
        localStorage.setItem(this.sessionKey, token);
    }

    /**
     * セッショントークンクリア
     */
    clearSessionToken() {
        localStorage.removeItem(this.sessionKey);
    }

    /**
     * 認証トークン取得（互換性のため）
     * @returns {string|null} 認証トークン
     */
    getToken() {
        return this.getSessionToken();
    }

    /**
     * 現在のユーザー情報取得
     * @returns {Object|null} ユーザー情報
     */
    getCurrentUser() {
        const userInfo = localStorage.getItem(CONFIG.STORAGE_KEYS.USER_INFO);
        return userInfo ? JSON.parse(userInfo) : null;
    }

    /**
     * 認証済みかチェック
     * @returns {boolean} 認証済みかどうか
     */
    isAuthenticated() {
        return !!this.getSessionToken();
    }
}

// グローバルに公開
window.AuthService = AuthService;
