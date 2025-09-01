let currentPage = '';
const routes = {
    '/': 'dashboard',
    '/login': 'login',
    '/dashboard': 'dashboard',
    '/attendance': 'attendance',
    '/leave-request': 'leave-request',
    '/admin': 'admin'
};

export function initRouter() {
    // 初期ページの設定
    handleRoute();
    
    // ブラウザの戻る/進むボタンのハンドリング
    window.addEventListener('popstate', handleRoute);
    
    // クリックイベントの委譲
    document.addEventListener('click', e => {
        if (e.target.matches('[data-route]')) {
            e.preventDefault();
            navigate(e.target.getAttribute('data-route'));
        }
    });
}

export function navigate(path) {
    window.history.pushState(null, '', path);
    handleRoute();
}

async function handleRoute() {
    const path = window.location.pathname;
    const page = routes[path] || 'not-found';
    
    if (page === currentPage) return;
    currentPage = page;
    
    // 認証チェック
    if (page !== 'login' && !isAuthenticated()) {
        navigate('/login');
        return;
    }
    
    // ページコンポーネントの読み込みと表示
    try {
        const component = await loadComponent(page);
        document.querySelector('#app').innerHTML = component;
    } catch (error) {
        console.error('Page loading error:', error);
        document.querySelector('#app').innerHTML = '<h1>エラーが発生しました</h1>';
    }
}

function isAuthenticated() {
    return !!localStorage.getItem('token');
}

async function loadComponent(page) {
    switch (page) {
        case 'dashboard':
            const { renderDashboard } = await import('../components/dashboard.js');
            return renderDashboard(JSON.parse(localStorage.getItem('user')));
            
        case 'attendance':
            const { renderAttendance } = await import('../components/attendance.js');
            return renderAttendance([]);
            
        case 'leave-request':
            const { renderLeaveRequest } = await import('../components/leaveRequest.js');
            return renderLeaveRequest();
            
        case 'admin':
            const { renderAdminPanel } = await import('../components/admin.js');
            return renderAdminPanel();
            
        case 'login':
            return `
                <div class="login-page">
                    <h1>ログイン</h1>
                    <form id="login-form">
                        <div class="form-group">
                            <label for="username">ユーザー名</label>
                            <input type="text" id="username" name="username" required>
                        </div>
                        <div class="form-group">
                            <label for="password">パスワード</label>
                            <input type="password" id="password" name="password" required>
                        </div>
                        <button type="submit" class="btn btn-primary">ログイン</button>
                    </form>
                </div>
            `;
            
        default:
            return '<h1>ページが見つかりません</h1>';
    }
}
