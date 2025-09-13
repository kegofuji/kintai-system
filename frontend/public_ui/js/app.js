// メインアプリケーション（設計書の画面遷移図完全準拠）
class App {
    constructor() {
        this.router = new Router();
        this.authService = new AuthService();
        this.currentUser = null;
        this.isInitialized = false;
        this.init();
    }

    /**
     * アプリケーション初期化
     */
    async init() {
        try {
            // セッション確認
            await this.checkSession();
            
            // ルート設定（設計書の画面遷移図通り）
            this.setupRoutes();
            
            // 初期画面表示
            this.router.navigate(window.location.hash || '#/login');
            
            // セッションタイムアウト監視（10分）
            this.startSessionMonitoring();
            
            // 初期化完了
            this.isInitialized = true;
            console.log('勤怠管理システムが初期化されました');
            
        } catch (error) {
            console.error('アプリケーション初期化エラー:', error);
            this.showError('アプリケーションの初期化に失敗しました');
        }
    }

    /**
     * セッション確認
     */
    async checkSession() {
        const token = this.authService.getToken();
        if (token) {
            try {
                const response = await this.authService.checkAuth();
                if (response.authenticated) {
                    this.currentUser = response.user;
                }
            } catch (error) {
                this.authService.logout();
            }
        }
    }

    /**
     * ルート設定（設計書の画面遷移図通り）
     */
    setupRoutes() {
        // L001: ログイン画面（全員）
        this.router.addRoute('/login', () => {
            this.showLogin();
        });
        
        // E001: 社員ダッシュボード（employee）
        this.router.addRoute('/employee/dashboard', () => {
            this.requireAuth('employee', () => {
                this.showEmployeeDashboard();
            });
        });
        
        // E002: 勤怠履歴画面（employee）
        this.router.addRoute('/employee/attendance', () => {
            this.requireAuth('employee', () => {
                this.showAttendanceHistory();
            });
        });
        
        // E003: 有給申請画面（employee）
        this.router.addRoute('/employee/leave', () => {
            this.requireAuth('employee', () => {
                this.showLeaveRequest();
            });
        });
        
        // E004: 打刻修正申請画面（employee）
        this.router.addRoute('/employee/adjustment', () => {
            this.requireAuth('employee', () => {
                this.showAdjustmentRequest();
            });
        });
        
        // A001: 管理者ダッシュボード（admin）
        this.router.addRoute('/admin/dashboard', () => {
            this.requireAuth('admin', () => {
                this.showAdminDashboard();
            });
        });
        
        // A002: 社員管理画面（admin）
        this.router.addRoute('/admin/employees', () => {
            this.requireAuth('admin', () => {
                this.showEmployeeManagement();
            });
        });
        
        // A003: 勤怠管理画面（admin）
        this.router.addRoute('/admin/attendance', () => {
            this.requireAuth('admin', () => {
                this.showAttendanceManagement();
            });
        });
        
        // A004: 申請承認画面（admin）
        this.router.addRoute('/admin/approvals', () => {
            this.requireAuth('admin', () => {
                this.showApprovalManagement();
            });
        });
        
        // A005: 有給管理画面（admin）
        this.router.addRoute('/admin/leave-management', () => {
            this.requireAuth('admin', () => {
                this.showLeaveManagement();
            });
        });
        
        // A006: レポート出力画面（admin）
        this.router.addRoute('/admin/reports', () => {
            this.requireAuth('admin', () => {
                this.showReportGeneration();
            });
        });

        // 旧ルートとの互換性（リダイレクト）
        this.router.addRoute('/dashboard', () => {
            if (this.currentUser && this.currentUser.role === 'admin') {
                this.router.navigate('/admin/dashboard');
            } else {
                this.router.navigate('/employee/dashboard');
            }
        });

        this.router.addRoute('/attendance', () => {
            this.router.navigate('/employee/attendance');
        });

        this.router.addRoute('/leave-request', () => {
            this.router.navigate('/employee/leave');
        });

        this.router.addRoute('/adjustment-request', () => {
            this.router.navigate('/employee/adjustment');
        });

        this.router.addRoute('/admin', () => {
            this.router.navigate('/admin/dashboard');
        });
    }

    /**
     * 認証チェック
     * @param {string} requiredRole - 必要なロール
     * @param {Function} callback - 認証成功時のコールバック
     */
    requireAuth(requiredRole, callback) {
        if (!this.currentUser) {
            this.router.navigate('/login');
            return;
        }
        
        // 退職者チェック（設計書仕様）
        if (this.currentUser.employmentStatus === 'retired') {
            this.showError('アクセス権限がありません');
            this.logout();
            return;
        }
        
        if (requiredRole === 'admin' && this.currentUser.role !== 'admin') {
            this.showError('管理者権限が必要です');
            return;
        }
        
        callback();
    }

    // 画面表示メソッド（各コンポーネント呼び出し）
    showLogin() {
        const container = document.getElementById('app');
        const loginComponent = new LoginComponent();
        container.innerHTML = loginComponent.render();
        loginComponent.setupEventListeners();
    }
    
    showEmployeeDashboard() {
        const container = document.getElementById('app');
        const dashboard = new DashboardComponent();
        container.innerHTML = dashboard.render();
        dashboard.setupEventListeners();
    }
    
    showAttendanceHistory() {
        const container = document.getElementById('app');
        const attendance = new AttendanceComponent();
        container.innerHTML = attendance.render();
    }
    
    showLeaveRequest() {
        const container = document.getElementById('app');
        const leaveRequest = new LeaveRequestComponent();
        container.innerHTML = leaveRequest.render();
    }
    
    showAdjustmentRequest() {
        const container = document.getElementById('app');
        const adjustmentRequest = new AdjustmentRequestComponent();
        container.innerHTML = adjustmentRequest.render();
    }
    
    showAdminDashboard() {
        const container = document.getElementById('app');
        const adminDashboard = new AdminDashboardComponent();
        container.innerHTML = adminDashboard.render();
    }
    
    showEmployeeManagement() {
        const container = document.getElementById('app');
        const employeeManagement = new EmployeeManagementComponent();
        container.innerHTML = employeeManagement.render();
    }
    
    showAttendanceManagement() {
        const container = document.getElementById('app');
        const attendanceManagement = new AttendanceComponent(); // 管理者用勤怠管理
        container.innerHTML = attendanceManagement.render();
    }
    
    showApprovalManagement() {
        const container = document.getElementById('app');
        const approvalManagement = new ApprovalManagementComponent();
        container.innerHTML = approvalManagement.render();
    }
    
    showLeaveManagement() {
        const container = document.getElementById('app');
        const leaveManagement = new LeaveRequestComponent(); // 管理者用有給管理
        container.innerHTML = leaveManagement.render();
    }
    
    showReportGeneration() {
        const container = document.getElementById('app');
        const reportGeneration = new AdminDashboardComponent(); // レポート生成画面
        container.innerHTML = reportGeneration.render();
    }
    
    /**
     * ログイン成功時の処理（設計書の画面遷移図通り）
     * @param {Object} loginResponse - ログイン応答
     */
    async onLoginSuccess(loginResponse) {
        this.currentUser = {
            employeeId: loginResponse.employeeId,
            employeeName: loginResponse.employeeName,
            role: loginResponse.role,
            employmentStatus: loginResponse.employmentStatus || 'active'
        };
        
        // ロール別ダッシュボード遷移
        if (loginResponse.role === 'admin') {
            this.router.navigate('/admin/dashboard');
        } else {
            this.router.navigate('/employee/dashboard');
        }
        
        this.showSuccess(`${loginResponse.employeeName}さん、ログインしました`);
    }
    
    /**
     * ログアウト処理（設計書：全画面から「ログアウト」でログイン画面へ戻る）
     */
    async logout() {
        try {
            await this.authService.logout();
        } catch (error) {
            // ログアウトエラーは無視
        }
        
        this.currentUser = null;
        this.router.navigate('/login');
        this.showInfo('ログアウトしました');
    }
    
    /**
     * セッションタイムアウト監視（設計書：10分）
     */
    startSessionMonitoring() {
        setInterval(async () => {
            if (this.currentUser) {
                try {
                    const response = await this.authService.checkAuth();
                    if (!response.authenticated) {
                        this.handleSessionTimeout();
                    }
                } catch (error) {
                    this.handleSessionTimeout();
                }
            }
        }, CONFIG.SESSION_CHECK_INTERVAL_MS);
    }
    
    /**
     * セッションタイムアウト処理（設計書仕様）
     */
    handleSessionTimeout() {
        this.currentUser = null;
        this.authService.logout();
        this.router.navigate('/login');
        this.showError('セッションがタイムアウトしました。再度ログインしてください。');
    }
    
    /**
     * メッセージ表示
     * @param {string} message - メッセージ
     * @param {string} type - メッセージタイプ
     */
    showMessage(message, type = 'info') {
        const container = document.getElementById('message-container');
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

    /**
     * 成功メッセージ表示
     * @param {string} message - 成功メッセージ
     */
    showSuccess(message) {
        this.showMessage(message, 'success');
    }

    /**
     * エラーメッセージ表示
     * @param {string} message - エラーメッセージ
     */
    showError(message) {
        this.showMessage(message, 'error');
    }

    /**
     * 情報メッセージ表示
     * @param {string} message - 情報メッセージ
     */
    showInfo(message) {
        this.showMessage(message, 'info');
    }

    /**
     * ローディング表示
     */
    showLoading() {
        document.getElementById('loading-overlay').classList.remove('d-none');
    }

    /**
     * ローディング非表示
     */
    hideLoading() {
        document.getElementById('loading-overlay').classList.add('d-none');
    }
}

// アプリケーション開始
document.addEventListener('DOMContentLoaded', () => {
    window.app = new App();
});

// グローバルに公開
window.App = App;