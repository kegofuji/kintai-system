/**
 * 認証サービス（セッション管理）
 */

class AuthService {
    constructor() {
        this.baseUrl = CONFIG.API_BASE_URL;
    }
    
    /**
     * ログイン
     */
    async login(employeeCode, password) {
        try {
            const response = await fetch(`${this.baseUrl}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    employeeCode,
                    password
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || 'ログインに失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('ログインエラー:', error);
            throw error;
        }
    }
    
    /**
     * ログアウト
     */
    async logout() {
        try {
            const response = await fetch(`${this.baseUrl}/auth/logout`, {
                method: 'POST',
                credentials: 'include'
            });
            
            const result = await response.json();
            return result;
        } catch (error) {
            console.error('ログアウトエラー:', error);
            throw error;
        }
    }
    
    /**
     * セッション確認
     */
    async getSession() {
        try {
            const response = await fetch(`${this.baseUrl}/auth/session`, {
                method: 'GET',
                credentials: 'include'
            });
            
            const result = await response.json();
            
            if (!response.ok && response.status === 401) {
                return null;
            }
            
            return result;
        } catch (error) {
            console.error('セッション確認エラー:', error);
            return null;
        }
    }
    
    /**
     * パスワード変更
     */
    async changePassword(oldPassword, newPassword) {
        try {
            const response = await fetch(`${this.baseUrl}/auth/change-password`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    oldPassword,
                    newPassword
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || 'パスワード変更に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('パスワード変更エラー:', error);
            throw error;
        }
    }
}

// グローバルに公開
window.AuthService = AuthService;