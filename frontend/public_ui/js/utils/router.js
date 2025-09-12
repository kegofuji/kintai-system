/**
 * SPAルーター（Vanilla JS）
 */

class Router {
    constructor() {
        this.routes = new Map();
        this.currentRoute = null;
        this.setupRoutes();
        this.init();
    }
    
    setupRoutes() {
        // 社員用ルート
        this.routes.set('attendance-history', {
            component: 'AttendanceComponent',
            method: 'loadHistory',
            title: '勤怠履歴'
        });
        
        this.routes.set('leave-request', {
            component: 'LeaveRequestComponent',
            method: 'loadLeaveRequest',
            title: '有給申請'
        });
        
        this.routes.set('adjustment-request', {
            component: 'AttendanceComponent',
            method: 'loadAdjustmentRequest',
            title: '打刻修正申請'
        });
        
        // 管理者用ルート
        this.routes.set('employee-management', {
            component: 'AdminComponent',
            method: 'loadEmployeeManagement',
            title: '社員管理',
            adminOnly: true
        });
        
        this.routes.set('attendance-management', {
            component: 'AdminComponent',
            method: 'loadAttendanceManagement',
            title: '勤怠管理',
            adminOnly: true
        });
        
        this.routes.set('request-approval', {
            component: 'AdminComponent',
            method: 'loadRequestApproval',
            title: '申請承認',
            adminOnly: true
        });
        
        this.routes.set('leave-management', {
            component: 'AdminComponent',
            method: 'loadLeaveManagement',
            title: '有給管理',
            adminOnly: true
        });
        
        this.routes.set('report-generation', {
            component: 'AdminComponent',
            method: 'loadReportGeneration',
            title: 'レポート出力',
            adminOnly: true
        });
    }
    
    init() {
        window.addEventListener('popstate', () => this.handleRouteChange());
    }
    
    navigate(route) {
        if (!this.routes.has(route)) {
            console.warn(`Route not found: ${route}`);
            return;
        }
        
        const routeConfig = this.routes.get(route);
        
        // 管理者専用ルートの権限チェック
        if (routeConfig.adminOnly && window.app?.currentUser?.role !== 'admin') {
            window.app?.showMessage('管理者権限が必要です', 'error');
            return;
        }
        
        this.currentRoute = route;
        this.loadRoute(routeConfig);
        
        // ナビゲーション状態更新
        this.updateNavigation(route);
    }
    
    async loadRoute(routeConfig) {
        try {
            const componentName = routeConfig.component;
            const methodName = routeConfig.method;
            
            // コンテンツエリア取得
            const contentArea = document.getElementById('dynamic-content') || 
                               document.getElementById('admin-content');
            
            if (!contentArea) {
                console.error('Content area not found');
                return;
            }
            
            // コンポーネントのインスタンス化と実行
            if (window[componentName]) {
                const component = new window[componentName](contentArea);
                if (component[methodName]) {
                    await component[methodName]();
                }
            } else {
                console.error(`Component not found: ${componentName}`);
            }
            
        } catch (error) {
            console.error('Route loading error:', error);
            if (window.app) {
                window.app.showMessage('ページの読み込みに失敗しました', 'error');
            }
        }
    }
    
    updateNavigation(activeRoute) {
        // ナビゲーションメニューのアクティブ状態更新
        document.querySelectorAll('[data-nav]').forEach(link => {
            if (link.dataset.nav === activeRoute) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }
    
    handleRouteChange() {
        // ブラウザバック/フォワード対応
        const path = window.location.pathname;
        const route = path.substring(1); // 先頭の / を除去
        
        if (this.routes.has(route)) {
            this.navigate(route);
        }
    }
}

window.Router = Router;