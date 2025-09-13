// SPA用ルーター（設計書の画面遷移図完全対応）
class Router {
    constructor() {
        this.routes = new Map();
        this.currentRoute = null;
        this.appContainer = document.getElementById('app');
        this.init();
    }

    /**
     * ルーター初期化
     */
    init() {
        // ハッシュ変更イベントを監視
        window.addEventListener('hashchange', () => this.handleRoute());
        
        // 初期ルートを処理
        this.handleRoute();
    }

    /**
     * ルート登録
     * @param {string} path - パス
     * @param {Function} handler - ハンドラー関数
     */
    addRoute(path, handler) {
        this.routes.set(path, handler);
    }

    /**
     * ルート遷移
     * @param {string} path - 遷移先パス
     */
    navigate(path) {
        window.location.hash = '#' + path;
        this.handleRoute();
    }

    /**
     * ルート処理
     */
    handleRoute() {
        const hash = window.location.hash.slice(1) || '/login';
        const handler = this.routes.get(hash);
        
        if (handler) {
            this.currentRoute = hash;
            try {
                handler();
            } catch (error) {
                console.error('Route handler error:', error);
                this.showError('画面の表示に失敗しました');
            }
        } else {
            // 404の場合はログイン画面に遷移
            this.navigate('/login');
        }
    }

    /**
     * 認証状態チェック
     * @returns {boolean} 認証済みかどうか
     */
    isAuthenticated() {
        return !!localStorage.getItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
    }

    /**
     * ユーザー情報取得
     * @returns {Object|null} ユーザー情報
     */
    getUserInfo() {
        const userInfo = localStorage.getItem(CONFIG.STORAGE_KEYS.USER_INFO);
        return userInfo ? JSON.parse(userInfo) : null;
    }

    /**
     * メッセージ表示
     * @param {string} message - メッセージ
     * @param {string} type - メッセージタイプ
     */
    showMessage(message, type = 'info') {
        const container = document.getElementById('message-container');
        if (container) {
            const messageDiv = document.createElement('div');
            messageDiv.className = `${type}-message`;
            messageDiv.textContent = message;
            
            container.appendChild(messageDiv);
            
            // 5秒後に自動削除
            setTimeout(() => {
                if (messageDiv.parentNode) {
                    messageDiv.parentNode.removeChild(messageDiv);
                }
            }, 5000);
        }
    }

    /**
     * エラー表示
     * @param {string} message - エラーメッセージ
     */
    showError(message) {
        this.showMessage(message, 'error');
    }

    /**
     * ローディング表示
     */
    showLoading() {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.classList.remove('d-none');
        }
    }

    /**
     * ローディング非表示
     */
    hideLoading() {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.classList.add('d-none');
        }
    }
}

// グローバルルーターインスタンス
const router = new Router();

// グローバルに公開
window.router = router;