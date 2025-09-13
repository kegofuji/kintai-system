// 勤怠管理画面コンポーネント（設計書画面ID A003完全準拠）
class AttendanceManagementComponent {
    constructor(container, app) {
        this.container = container;
        this.app = app;
        this.adminService = new AdminService();
        this.attendanceData = [];
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
                                <a class="nav-link active" href="#/admin/attendance">勤怠管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/approvals">申請承認</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/leave-management">有給管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/reports">レポート出力</a>
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
                            <h1 class="mt-4 mb-4">勤怠管理</h1>
                        </div>
                    </div>
                    
                    <div class="attendance-management">
                        <div class="filter-section mb-3">
                            <div class="row align-items-end">
                                <div class="col-md-3">
                                    <label for="employee_filter" class="form-label">社員選択</label>
                                    <select id="employee_filter" class="form-select">
                                        <option value="">全社員</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label for="month_filter" class="form-label">対象月</label>
                                    <input type="month" id="month_filter" class="form-control" 
                                           value="${DateUtil.getCurrentYearMonth()}">
                                </div>
                                <div class="col-md-2">
                                    <button type="button" id="search_btn" class="btn btn-primary">
                                        検索
                                    </button>
                                </div>
                                <div class="col-md-2">
                                    <button type="button" id="bulk_update_btn" class="btn btn-warning">
                                        一括更新
                                    </button>
                                </div>
                            </div>
                        </div>
                        
                        <div class="table-responsive">
                            <table class="table table-hover" id="attendance_table">
                                <thead>
                                    <tr>
                                        <th>社員名</th>
                                        <th>日付</th>
                                        <th>出勤時刻</th>
                                        <th>退勤時刻</th>
                                        <th>遅刻時間</th>
                                        <th>早退時間</th>
                                        <th>残業時間</th>
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
        `;
        
        this.loadEmployees();
        this.loadAttendanceData();
    }
    
    attachEventListeners() {
        const searchBtn = document.getElementById('search_btn');
        const bulkUpdateBtn = document.getElementById('bulk_update_btn');
        
        searchBtn.addEventListener('click', () => {
            this.loadAttendanceData();
        });
        
        bulkUpdateBtn.addEventListener('click', () => {
            this.showBulkUpdateModal();
        });
    }
    
    async loadEmployees() {
        try {
            const response = await this.adminService.getEmployees();
            
            if (response.success) {
                const employeeSelect = document.getElementById('employee_filter');
                employeeSelect.innerHTML = '<option value="">全社員</option>';
                
                response.data.forEach(employee => {
                    const option = document.createElement('option');
                    option.value = employee.employeeId;
                    option.textContent = employee.employeeName;
                    employeeSelect.appendChild(option);
                });
            }
            
        } catch (error) {
            this.app.showError('社員データの取得に失敗しました');
        }
    }
    
    async loadAttendanceData() {
        try {
            this.app.showLoading();
            
            const employeeId = document.getElementById('employee_filter').value;
            const yearMonth = document.getElementById('month_filter').value;
            
            const response = await this.adminService.getAttendanceData({
                employeeId: employeeId || undefined,
                yearMonth: yearMonth
            });
            
            if (response.success) {
                this.attendanceData = response.data;
                this.renderAttendanceTable(this.attendanceData);
            } else {
                this.app.showError('勤怠データの取得に失敗しました');
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    renderAttendanceTable(attendanceList) {
        const tbody = document.querySelector('#attendance_table tbody');
        
        if (attendanceList.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center text-muted">該当する勤怠データがありません</td>
                </tr>
            `;
            return;
        }
        
        tbody.innerHTML = attendanceList.map(record => `
            <tr>
                <td>${record.employeeName}</td>
                <td>${DateUtil.formatDateJP(record.attendanceDate)}</td>
                <td>
                    <input type="time" class="form-control form-control-sm" 
                           value="${record.clockInTime || ''}" 
                           data-field="clockInTime" 
                           data-record-id="${record.attendanceId}">
                </td>
                <td>
                    <input type="time" class="form-control form-control-sm" 
                           value="${record.clockOutTime || ''}" 
                           data-field="clockOutTime" 
                           data-record-id="${record.attendanceId}">
                </td>
                <td class="${record.lateMinutes > 0 ? 'text-warning' : ''}">
                    ${Formatter.minutesToHHMM(record.lateMinutes)}
                </td>
                <td class="${record.earlyLeaveMinutes > 0 ? 'text-warning' : ''}">
                    ${Formatter.minutesToHHMM(record.earlyLeaveMinutes)}
                </td>
                <td class="${record.overtimeMinutes > 0 ? 'text-primary' : ''}">
                    ${Formatter.minutesToHHMM(record.overtimeMinutes)}
                </td>
                <td>
                    <span class="status-${record.attendanceStatus}">
                        ${Formatter.formatAttendanceStatus(record.attendanceStatus)}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" 
                            onclick="this.updateAttendance(${record.attendanceId})">
                        更新
                    </button>
                </td>
            </tr>
        `).join('');
    }
    
    async updateAttendance(attendanceId) {
        const row = document.querySelector(`tr[data-attendance-id="${attendanceId}"]`);
        if (!row) return;
        
        const clockInTime = row.querySelector('input[data-field="clockInTime"]').value;
        const clockOutTime = row.querySelector('input[data-field="clockOutTime"]').value;
        
        try {
            this.app.showLoading();
            
            const response = await this.adminService.updateAttendance(attendanceId, {
                clockInTime: clockInTime || null,
                clockOutTime: clockOutTime || null
            });
            
            if (response.success) {
                this.app.showSuccess('勤怠データを更新しました');
                this.loadAttendanceData();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    showBulkUpdateModal() {
        // 一括更新モーダル（簡易実装）
        this.app.showInfo('一括更新機能は開発中です');
    }
}

// グローバルに公開
window.AttendanceManagementComponent = AttendanceManagementComponent;
