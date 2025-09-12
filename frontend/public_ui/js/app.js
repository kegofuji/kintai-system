/**
 * メインアプリケーションクラス（Vanilla JS SPA）
 * 勤怠管理システム - フロントエンドメイン制御
 */

class App {
    constructor() {
        this.router = new Router();
        this.authService = new AuthService();
        this.attendanceService = new AttendanceService();
        this.requestService = new RequestService();
        this.adminService = new AdminService();
        
        this.currentUser = null;
        this.sessionTimer = null;
        
        this.init();
    }
    
    /**
     * アプリケーション初期化
     */
    async init() {
        try {
            // ローディング表示
            this.showLoading(true);
            
            // イベントリスナー設定
            this.setupEventListeners();
            
            // セッション確認
            await this.checkSession();
            
            // 初期画面表示
            this.showInitialScreen();
            
            // ローディング非表示
            this.showLoading(false);
            
            console.log('勤怠管理システム初期化完了');
            
        } catch (error) {
            console.error('アプリケーション初期化エラー:', error);
            this.showMessage('システムの初期化に失敗しました', 'error');
            this.showLoading(false);
        }
    }
    
    /**
     * イベントリスナー設定
     */
    setupEventListeners() {
        // ログインフォーム
        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }
        
        // 出退勤ボタン
        const clockInBtn = document.getElementById('clock-in-btn');
        const clockOutBtn = document.getElementById('clock-out-btn');
        
        if (clockInBtn) {
            clockInBtn.addEventListener('click', () => this.handleClockIn());
        }
        if (clockOutBtn) {
            clockOutBtn.addEventListener('click', () => this.handleClockOut());
        }
        
        // ログアウトボタン
        const logoutBtn = document.getElementById('logout-btn');
        const adminLogoutBtn = document.getElementById('admin-logout-btn');
        
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.handleLogout());
        }
        if (adminLogoutBtn) {
            adminLogoutBtn.addEventListener('click', () => this.handleLogout());
        }
        
        // ナビゲーションメニュー
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-nav]')) {
                e.preventDefault();
                const route = e.target.dataset.nav;
                this.router.navigate(route);
            }
        });
        
        // モーダルクローズ
        const modalOverlay = document.getElementById('modal-overlay');
        const modalCloseBtn = document.getElementById('modal-close-btn');
        const modalCancelBtn = document.getElementById('modal-cancel-btn');
        
        if (modalOverlay) {
            modalOverlay.addEventListener('click', (e) => {
                if (e.target === modalOverlay) {
                    this.hideModal();
                }
            });
        }
        if (modalCloseBtn) {
            modalCloseBtn.addEventListener('click', () => this.hideModal());
        }
        if (modalCancelBtn) {
            modalCancelBtn.addEventListener('click', () => this.hideModal());
        }
        
        // キーボードショートカット
        document.addEventListener('keydown', (e) => this.handleKeyboard(e));
        
        // ページ離脱時の確認
        window.addEventListener('beforeunload', (e) => {
            if (this.currentUser) {
                e.returnValue = '作業中のデータが失われる可能性があります。';
            }
        });
        
        // セッションタイムアウト警告
        this.setupSessionWarning();
    }
    
    /**
     * セッション確認
     */
    async checkSession() {
        try {
            const session = await this.authService.getSession();
            if (session && session.success) {
                this.currentUser = session.data;
                this.startSessionTimer(session.data.remainingTime);
                return true;
            }
        } catch (error) {
            console.log('セッション確認エラー（初回起動時は正常）:', error);
        }
        return false;
    }
    
    /**
     * 初期画面表示
     */
    showInitialScreen() {
        if (this.currentUser) {
            // ログイン済みの場合、適切なダッシュボードを表示
            if (this.currentUser.role === 'admin') {
                this.showScreen('admin-dashboard');
                this.updateUserInfo('admin-user-name', 'admin-session-timer');
                this.loadAdminDashboard();
            } else {
                this.showScreen('employee-dashboard');
                this.updateUserInfo('user-name', 'session-timer');
                this.loadEmployeeDashboard();
            }
        } else {
            // 未ログインの場合、ログイン画面を表示
            this.showScreen('login-screen');
        }
    }
    
    /**
     * ログイン処理
     */
    async handleLogin(event) {
        event.preventDefault();
        
        try {
            const formData = new FormData(event.target);
            const employeeCode = formData.get('employeeCode').trim();
            const password = formData.get('password');
            
            // バリデーション
            if (!this.validateLoginForm(employeeCode, password)) {
                return;
            }
            
            // ローディング表示
            this.showLoading(true, 'ログイン中...');
            
            // ログイン要求
            const result = await this.authService.login(employeeCode, password);
            
            if (result && result.success) {
                this.currentUser = result.data;
                this.showMessage('ログインしました', 'success');
                
                // セッションタイマー開始
                this.startSessionTimer(CONFIG.SESSION_TIMEOUT_MINUTES * 60);
                
                // 適切な画面に遷移
                if (this.currentUser.role === 'admin') {
                    this.showScreen('admin-dashboard');
                    this.updateUserInfo('admin-user-name', 'admin-session-timer');
                    this.loadAdminDashboard();
                } else {
                    this.showScreen('employee-dashboard');
                    this.updateUserInfo('user-name', 'session-timer');
                    this.loadEmployeeDashboard();
                }
                
                // ログインフォームリセット
                event.target.reset();
                this.clearFormErrors();
                
            } else {
                this.showMessage(result?.message || 'ログインに失敗しました', 'error');
            }
            
        } catch (error) {
            console.error('ログインエラー:', error);
            this.showMessage('ログイン処理中にエラーが発生しました', 'error');
        } finally {
            this.showLoading(false);
        }
    }
    
    /**
     * ログアウト処理
     */
    async handleLogout() {
        try {
            const confirmed = await this.showConfirm('ログアウトしますか？');
            if (!confirmed) return;
            
            this.showLoading(true, 'ログアウト中...');
            
            await this.authService.logout();
            
            this.currentUser = null;
            this.stopSessionTimer();
            
            this.showScreen('login-screen');
            this.showMessage('ログアウトしました', 'info');
            
        } catch (error) {
            console.error('ログアウトエラー:', error);
            this.showMessage('ログアウト処理中にエラーが発生しました', 'error');
        } finally {
            this.showLoading(false);
        }
    }
    
    /**
     * 出勤打刻処理
     */
    async handleClockIn() {
        try {
            if (!this.currentUser) return;
            
            const result = await this.attendanceService.clockIn(this.currentUser.employeeId);
            
            if (result && result.success) {
                this.showMessage(result.message || '出勤打刻が完了しました', 'success');
                await this.updateTodayStatus();
            } else {
                this.showMessage(result?.message || '出勤打刻に失敗しました', 'error');
            }
            
        } catch (error) {
            console.error('出勤打刻エラー:', error);
            this.showMessage('出勤打刻処理中にエラーが発生しました', 'error');
        }
    }
    
    /**
     * 退勤打刻処理
     */
    async handleClockOut() {
        try {
            if (!this.currentUser) return;
            
            const result = await this.attendanceService.clockOut(this.currentUser.employeeId);
            
            if (result && result.success) {
                this.showMessage(result.message || '退勤打刻が完了しました', 'success');
                await this.updateTodayStatus();
            } else {
                this.showMessage(result?.message || '退勤打刻に失敗しました', 'error');
            }
            
        } catch (error) {
            console.error('退勤打刻エラー:', error);
            this.showMessage('退勤打刻処理中にエラーが発生しました', 'error');
        }
    }
    
    /**
     * 社員ダッシュボード読み込み
     */
    async loadEmployeeDashboard() {
        try {
            const dashboard = new Dashboard('dynamic-content');
            await dashboard.loadEmployeeDashboard();
            
            // 当日の勤務状況更新
            await this.updateTodayStatus();
            
        } catch (error) {
            console.error('社員ダッシュボード読み込みエラー:', error);
            this.showMessage('ダッシュボードの読み込みに失敗しました', 'error');
        }
    }
    
    /**
     * 管理者ダッシュボード読み込み
     */
    async loadAdminDashboard() {
        try {
            const adminComponent = new AdminComponent('admin-content');
            await adminComponent.loadAdminDashboard();
            
        } catch (error) {
            console.error('管理者ダッシュボード読み込みエラー:', error);
            this.showMessage('管理者ダッシュボードの読み込みに失敗しました', 'error');
        }
    }
    
    /**
     * 当日の勤務状況更新
     */
    async updateTodayStatus() {
        try {
            if (!this.currentUser) return;
            
            const status = await this.attendanceService.getTodayStatus(this.currentUser.employeeId);
            
            if (status && status.success) {
                const data = status.data;
                
                const clockInElement = document.getElementById('today-clock-in');
                const clockOutElement = document.getElementById('today-clock-out');
                const clockInBtn = document.getElementById('clock-in-btn');
                const clockOutBtn = document.getElementById('clock-out-btn');
                
                if (clockInElement) {
                    clockInElement.textContent = data.clockInTime ? 
                        DateUtil.formatTimeOnly(data.clockInTime) : '--:--';
                }
                
                if (clockOutElement) {
                    clockOutElement.textContent = data.clockOutTime ? 
                        DateUtil.formatTimeOnly(data.clockOutTime) : '--:--';
                }
                
                // ボタン状態制御
                if (clockInBtn && clockOutBtn) {
                    if (data.hasClockOut) {
                        // 退勤済み
                        clockInBtn.disabled = true;
                        clockOutBtn.disabled = true;
                    } else if (data.hasClockIn) {
                        // 出勤済み、退勤待ち
                        clockInBtn.disabled = true;
                        clockOutBtn.disabled = false;
                    } else {
                        // 未出勤
                        clockInBtn.disabled = false;
                        clockOutBtn.disabled = true;
                    }
                }
            }
            
        } catch (error) {
            console.error('当日状況更新エラー:', error);
        }
    }
    
    /**
     * ログインフォームバリデーション
     */
    validateLoginForm(employeeCode, password) {
        let isValid = true;
        
        // 社員ID検証
        if (!employeeCode) {
            this.showFieldError('employee-code-error', '社員IDは必須です');
            isValid = false;
        } else if (!Validator.validateEmployeeCode(employeeCode)) {
            this.showFieldError('employee-code-error', '社員IDは3-10文字の半角英数字で入力してください');
            isValid = false;
        } else {
            this.clearFieldError('employee-code-error');
        }
        
        // パスワード検証
        if (!password) {
            this.showFieldError('password-error', 'パスワードは必須です');
            isValid = false;
        } else if (password.length < 8) {
            this.showFieldError('password-error', 'パスワードは8文字以上で入力してください');
            isValid = false;
        } else {
            this.clearFieldError('password-error');
        }
        
        return isValid;
    }
    
    /**
     * セッションタイマー開始
     */
    startSessionTimer(remainingSeconds) {
        this.stopSessionTimer();
        
        const updateTimer = () => {
            if (remainingSeconds <= 0) {
                this.handleSessionTimeout();
                return;
            }
            
            const minutes = Math.floor(remainingSeconds / 60);
            const seconds = remainingSeconds % 60;
            const timeText = `${minutes}:${seconds.toString().padStart(2, '0')}`;
            
            const employeeTimer = document.getElementById('session-timer');
            const adminTimer = document.getElementById('admin-session-timer');
            
            if (employeeTimer) employeeTimer.textContent = timeText;
            if (adminTimer) adminTimer.textContent = timeText;
            
            // 5分以下で警告色
            if (remainingSeconds <= 300) {
                if (employeeTimer) employeeTimer.style.backgroundColor = 'var(--danger-color)';
                if (adminTimer) adminTimer.style.backgroundColor = 'var(--danger-color)';
            }
            
            remainingSeconds--;
        };
        
        updateTimer();
        this.sessionTimer = setInterval(updateTimer, 1000);
    }
    
    /**
     * セッションタイマー停止
     */
    stopSessionTimer() {
        if (this.sessionTimer) {
            clearInterval(this.sessionTimer);
            this.sessionTimer = null;
        }
    }
    
    /**
     * セッションタイムアウト処理
     */
    async handleSessionTimeout() {
        this.stopSessionTimer();
        this.currentUser = null;
        
        this.showScreen('login-screen');
        this.showMessage('セッションがタイムアウトしました。再度ログインしてください', 'warning');
    }
    
    /**
     * セッション警告設定
     */
    setupSessionWarning() {
        // 1分前に警告表示
        setTimeout(() => {
            if (this.currentUser && this.sessionTimer) {
                this.showMessage('セッションが間もなく終了します', 'warning');
            }
        }, (CONFIG.SESSION_TIMEOUT_MINUTES - 1) * 60 * 1000);
    }
    
    /**
     * ユーザー情報表示更新
     */
    updateUserInfo(nameElementId, timerElementId) {
        const nameElement = document.getElementById(nameElementId);
        if (nameElement && this.currentUser) {
            nameElement.textContent = this.currentUser.employeeName || 'ユーザー';
        }
    }
    
    /**
     * 画面表示制御
     */
    showScreen(screenId) {
        // 全ての画面を非表示
        document.querySelectorAll('.screen').forEach(screen => {
            screen.style.display = 'none';
            screen.classList.remove('active');
        });