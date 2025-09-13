// 管理者ダッシュボードコンポーネント（設計書画面ID A001完全準拠）
class AdminDashboard {
    constructor(container, app) {
        this.container = container;
        this.app = app;
    }
    
    render() {
        this.container.innerHTML = `
            <nav class="navbar navbar-expand-lg navbar-custom">
                <div class="container-fluid">
                    <span class="navbar-brand">勤怠管理システム - 管理者</span>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarNav">
                        <ul class="navbar-nav me-auto">
                            <li class="nav-item">
                                <a class="nav-link active" href="#/admin/dashboard">ダッシュボード</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/employees" id="employee_mgmt_link">社員管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/attendance" id="attendance_mgmt_link">勤怠管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/approvals" id="approval_mgmt_link">申請承認</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/leave-management" id="leave_mgmt_link">有給管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/reports" id="report_link">レポート出力</a>
                            </li>
                        </ul>
                        <ul class="navbar-nav">
                            <li class="nav-item">
                                <span class="nav-link">${this.app.currentUser.employeeName}さん（管理者）</span>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#" onclick="app.logout()">ログアウト</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
            
            <div class="admin-dashboard">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-12">
                            <h1 class="mt-4 mb-4">管理者ダッシュボード</h1>
                            <p class="lead">勤怠管理システムの各種管理機能にアクセスできます。</p>
                        </div>
                    </div>
                    
                    <div class="admin-menu">
                        <div class="menu-card" onclick="app.router.navigate('/admin/employees')">
                            <div class="icon">👥</div>
                            <h3>社員管理</h3>
                            <p>社員の追加・編集・退職処理を行います</p>
                        </div>
                        
                        <div class="menu-card" onclick="app.router.navigate('/admin/attendance')">
                            <div class="icon">📊</div>
                            <h3>勤怠管理</h3>
                            <p>全社員の勤怠データを管理・編集します</p>
                        </div>
                        
                        <div class="menu-card" onclick="app.router.navigate('/admin/approvals')">
                            <div class="icon">✅</div>
                            <h3>申請承認</h3>
                            <p>有給申請・打刻修正・月末申請を承認・却下します</p>
                        </div>
                        
                        <div class="menu-card" onclick="app.router.navigate('/admin/leave-management')">
                            <div class="icon">🏖️</div>
                            <h3>有給管理</h3>
                            <p>社員の有給残日数を確認・調整します</p>
                        </div>
                        
                        <div class="menu-card" onclick="app.router.navigate('/admin/reports')">
                            <div class="icon">📄</div>
                            <h3>レポート出力</h3>
                            <p>月次勤怠レポートをPDF形式で出力します</p>
                        </div>
                        
                        <div class="menu-card" onclick="this.showIntegrityCheck()">
                            <div class="icon">🔍</div>
                            <h3>勤怠整合性チェック</h3>
                            <p>打刻漏れ・データ不整合をチェックします</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
    
    showIntegrityCheck() {
        // 勤怠整合性チェック機能（設計書の用例9）
        this.app.showInfo('勤怠整合性チェック機能は開発中です');
    }
}

// グローバルに公開
window.AdminDashboard = AdminDashboard;