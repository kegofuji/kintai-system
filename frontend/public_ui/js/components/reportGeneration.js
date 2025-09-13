// レポート出力画面コンポーネント（設計書画面ID A006完全準拠）
class ReportGenerationComponent {
    constructor(container, app) {
        this.container = container;
        this.app = app;
        this.attendanceService = new AttendanceService();
        this.adminService = new AdminService();
    }
    
    render() {
        return `
            <nav class="navbar navbar-expand-lg navbar-custom">
                <div class="container-fluid">
                    <span class="navbar-brand">勤怠管理システム - 管理者</span>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarNav">
                        <ul class="navbar-nav me-auto">
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/dashboard">ダッシュボード</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/employees">社員管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/attendance">勤怠管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/approvals">申請承認</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/leave-management">有給管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="#/admin/reports">レポート出力</a>
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
                            <h1 class="mt-4 mb-4">レポート出力</h1>
                        </div>
                    </div>
                    
                    <div class="report-generation">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">月次勤怠レポート</h5>
                                    </div>
                                    <div class="card-body">
                                        <form id="monthlyReportForm">
                                            <div class="mb-3">
                                                <label for="report_month" class="form-label">対象月</label>
                                                <input type="month" id="report_month" class="form-control" 
                                                       value="${DateUtil.getCurrentYearMonth()}" required>
                                            </div>
                                            <div class="mb-3">
                                                <label for="report_employee" class="form-label">対象社員</label>
                                                <select id="report_employee" class="form-select">
                                                    <option value="">全社員</option>
                                                </select>
                                            </div>
                                            <button type="submit" class="btn btn-primary" id="generate_monthly_btn">
                                                PDF生成
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">週次勤怠レポート</h5>
                                    </div>
                                    <div class="card-body">
                                        <form id="weeklyReportForm">
                                            <div class="mb-3">
                                                <label for="report_week_start" class="form-label">開始日</label>
                                                <input type="date" id="report_week_start" class="form-control" required>
                                            </div>
                                            <div class="mb-3">
                                                <label for="report_week_employee" class="form-label">対象社員</label>
                                                <select id="report_week_employee" class="form-select">
                                                    <option value="">全社員</option>
                                                </select>
                                            </div>
                                            <button type="submit" class="btn btn-primary" id="generate_weekly_btn">
                                                PDF生成
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row mt-4">
                            <div class="col-md-6">
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">日次勤怠レポート</h5>
                                    </div>
                                    <div class="card-body">
                                        <form id="dailyReportForm">
                                            <div class="mb-3">
                                                <label for="report_date" class="form-label">対象日</label>
                                                <input type="date" id="report_date" class="form-control" 
                                                       value="${DateUtil.getCurrentDate()}" required>
                                            </div>
                                            <div class="mb-3">
                                                <label for="report_daily_employee" class="form-label">対象社員</label>
                                                <select id="report_daily_employee" class="form-select">
                                                    <option value="">全社員</option>
                                                </select>
                                            </div>
                                            <button type="submit" class="btn btn-primary" id="generate_daily_btn">
                                                PDF生成
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">有給取得レポート</h5>
                                    </div>
                                    <div class="card-body">
                                        <form id="leaveReportForm">
                                            <div class="mb-3">
                                                <label for="leave_year" class="form-label">対象年</label>
                                                <select id="leave_year" class="form-select" required>
                                                    <option value="${new Date().getFullYear()}">${new Date().getFullYear()}年</option>
                                                    <option value="${new Date().getFullYear() - 1}">${new Date().getFullYear() - 1}年</option>
                                                </select>
                                            </div>
                                            <div class="mb-3">
                                                <label for="leave_employee" class="form-label">対象社員</label>
                                                <select id="leave_employee" class="form-select">
                                                    <option value="">全社員</option>
                                                </select>
                                            </div>
                                            <button type="submit" class="btn btn-primary" id="generate_leave_btn">
                                                PDF生成
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row mt-4">
                            <div class="col-12">
                                <div class="card">
                                    <div class="card-header">
                                        <h5 class="mb-0">生成履歴</h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="table-responsive">
                                            <table class="table table-hover" id="report_history_table">
                                                <thead>
                                                    <tr>
                                                        <th>生成日時</th>
                                                        <th>レポート種別</th>
                                                        <th>対象期間</th>
                                                        <th>対象社員</th>
                                                        <th>ステータス</th>
                                                        <th>操作</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <!-- 動的に生成 -->
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        this.loadEmployees();
        this.loadReportHistory();
    }
    
    attachEventListeners() {
        const monthlyForm = document.getElementById('monthlyReportForm');
        const weeklyForm = document.getElementById('weeklyReportForm');
        const dailyForm = document.getElementById('dailyReportForm');
        const leaveForm = document.getElementById('leaveReportForm');
        
        monthlyForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.generateMonthlyReport();
        });
        
        weeklyForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.generateWeeklyReport();
        });
        
        dailyForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.generateDailyReport();
        });
        
        leaveForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.generateLeaveReport();
        });
        
        // 週の開始日を設定
        this.setWeekStartDate();
    }
    
    setWeekStartDate() {
        const today = new Date();
        const dayOfWeek = today.getDay();
        const monday = new Date(today);
        monday.setDate(today.getDate() - dayOfWeek + 1);
        
        document.getElementById('report_week_start').value = DateUtil.formatDate(monday);
    }
    
    async loadEmployees() {
        try {
            const response = await this.adminService.getEmployees();
            
            if (response.success) {
                const selects = [
                    'report_employee',
                    'report_week_employee', 
                    'report_daily_employee',
                    'leave_employee'
                ];
                
                selects.forEach(selectId => {
                    const select = document.getElementById(selectId);
                    select.innerHTML = '<option value="">全社員</option>';
                    
                    response.data.forEach(employee => {
                        const option = document.createElement('option');
                        option.value = employee.employeeId;
                        option.textContent = employee.employeeName;
                        select.appendChild(option);
                    });
                });
            }
            
        } catch (error) {
            this.app.showError('社員データの取得に失敗しました');
        }
    }
    
    async generateMonthlyReport() {
        const yearMonth = document.getElementById('report_month').value;
        const employeeId = document.getElementById('report_employee').value;
        
        try {
            this.app.showLoading();
            
            const response = await this.attendanceService.generateReport(yearMonth, employeeId);
            
            if (response.success) {
                this.app.showSuccess('月次レポートを生成しました');
                this.downloadReport(response.data.reportUrl);
                this.loadReportHistory();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    async generateWeeklyReport() {
        const startDate = document.getElementById('report_week_start').value;
        const employeeId = document.getElementById('report_week_employee').value;
        
        try {
            this.app.showLoading();
            
            const response = await this.attendanceService.generateWeeklyReport(startDate, employeeId);
            
            if (response.success) {
                this.app.showSuccess('週次レポートを生成しました');
                this.downloadReport(response.data.reportUrl);
                this.loadReportHistory();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    async generateDailyReport() {
        const date = document.getElementById('report_date').value;
        const employeeId = document.getElementById('report_daily_employee').value;
        
        try {
            this.app.showLoading();
            
            const response = await this.attendanceService.generateDailyReport(date, employeeId);
            
            if (response.success) {
                this.app.showSuccess('日次レポートを生成しました');
                this.downloadReport(response.data.reportUrl);
                this.loadReportHistory();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    async generateLeaveReport() {
        const year = document.getElementById('leave_year').value;
        const employeeId = document.getElementById('leave_employee').value;
        
        try {
            this.app.showLoading();
            
            const response = await this.attendanceService.generateLeaveReport(year, employeeId);
            
            if (response.success) {
                this.app.showSuccess('有給取得レポートを生成しました');
                this.downloadReport(response.data.reportUrl);
                this.loadReportHistory();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    downloadReport(reportUrl) {
        const link = document.createElement('a');
        link.href = reportUrl;
        link.download = '';
        link.target = '_blank';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
    
    async loadReportHistory() {
        try {
            const response = await this.adminService.getReportHistory();
            
            if (response.success) {
                this.renderReportHistory(response.data);
            }
            
        } catch (error) {
            // 履歴の取得に失敗してもエラー表示しない
            console.error('Report history load failed:', error);
        }
    }
    
    renderReportHistory(history) {
        const tbody = document.querySelector('#report_history_table tbody');
        
        if (history.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted">生成履歴がありません</td>
                </tr>
            `;
            return;
        }
        
        tbody.innerHTML = history.map(record => `
            <tr>
                <td>${DateUtil.formatDateJP(record.generatedAt)}</td>
                <td>${Formatter.formatRequestType(record.reportType)}</td>
                <td>${record.period}</td>
                <td>${record.employeeName || '全社員'}</td>
                <td>
                    <span class="status-${record.status === 'completed' ? 'success' : 'warning'}">
                        ${record.status === 'completed' ? '完了' : '処理中'}
                    </span>
                </td>
                <td>
                    ${record.status === 'completed' ? 
                        `<a href="${record.downloadUrl}" class="btn btn-sm btn-outline-primary" target="_blank">
                            ダウンロード
                        </a>` : 
                        '<span class="text-muted">処理中...</span>'
                    }
                </td>
            </tr>
        `).join('');
    }
}

// グローバルに公開
window.ReportGenerationComponent = ReportGenerationComponent;
