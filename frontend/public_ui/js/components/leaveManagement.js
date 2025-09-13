// 有給管理画面コンポーネント（設計書画面ID A005完全準拠）
class LeaveManagementComponent {
    constructor(container, app) {
        this.container = container;
        this.app = app;
        this.adminService = new AdminService();
        this.employees = [];
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
                                <a class="nav-link active" href="#/admin/leave-management">有給管理</a>
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
                            <h1 class="mt-4 mb-4">有給管理</h1>
                        </div>
                    </div>
                    
                    <div class="leave-management">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <div class="search-section">
                                <div class="input-group">
                                    <input type="text" class="form-control" id="employee_search" 
                                           placeholder="社員名で検索">
                                    <button class="btn btn-outline-secondary" type="button" id="search_btn">
                                        検索
                                    </button>
                                </div>
                            </div>
                            <button class="btn btn-primary" id="bulk_adjust_btn">
                                一括調整
                            </button>
                        </div>
                        
                        <div class="table-responsive">
                            <table class="table table-hover" id="leave_table">
                                <thead>
                                    <tr>
                                        <th>社員ID</th>
                                        <th>氏名</th>
                                        <th>有給残日数</th>
                                        <th>今年度取得日数</th>
                                        <th>最終取得日</th>
                                        <th>次回付与予定</th>
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
    }
    
    attachEventListeners() {
        const searchBtn = document.getElementById('search_btn');
        const bulkAdjustBtn = document.getElementById('bulk_adjust_btn');
        const searchInput = document.getElementById('employee_search');
        
        searchBtn.addEventListener('click', () => {
            this.searchEmployees();
        });
        
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.searchEmployees();
            }
        });
        
        bulkAdjustBtn.addEventListener('click', () => {
            this.showBulkAdjustModal();
        });
    }
    
    async loadEmployees() {
        try {
            this.app.showLoading();
            
            const response = await this.adminService.getEmployees();
            
            if (response.success) {
                this.employees = response.data;
                this.renderLeaveTable(this.employees);
            } else {
                this.app.showError('社員データの取得に失敗しました');
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    searchEmployees() {
        const searchTerm = document.getElementById('employee_search').value.trim().toLowerCase();
        
        if (!searchTerm) {
            this.renderLeaveTable(this.employees);
            return;
        }
        
        const filteredEmployees = this.employees.filter(employee => 
            employee.employeeName.toLowerCase().includes(searchTerm) ||
            employee.employeeCode.toLowerCase().includes(searchTerm)
        );
        
        this.renderLeaveTable(filteredEmployees);
    }
    
    renderLeaveTable(employees) {
        const tbody = document.querySelector('#leave_table tbody');
        
        if (employees.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted">該当する社員が見つかりません</td>
                </tr>
            `;
            return;
        }
        
        tbody.innerHTML = employees.map(employee => `
            <tr>
                <td>${employee.employeeCode}</td>
                <td>${employee.employeeName}</td>
                <td class="text-primary">
                    <strong>${employee.remainingLeaveDays}日</strong>
                </td>
                <td>${employee.usedLeaveDays || 0}日</td>
                <td>${employee.lastLeaveDate ? DateUtil.formatDateJP(employee.lastLeaveDate) : '-'}</td>
                <td>${employee.nextGrantDate ? DateUtil.formatDateJP(employee.nextGrantDate) : '-'}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" 
                            onclick="this.adjustLeaveDays(${employee.employeeId}, '${employee.employeeName}', ${employee.remainingLeaveDays})">
                        調整
                    </button>
                </td>
            </tr>
        `).join('');
    }
    
    adjustLeaveDays(employeeId, employeeName, currentDays) {
        const adjustment = prompt(`${employeeName}の有給残日数を調整してください\n現在: ${currentDays}日\n調整値（例: +5, -3）:`);
        
        if (!adjustment) return;
        
        const match = adjustment.match(/^([+-]?)(\d+)$/);
        if (!match) {
            this.app.showError('調整値は「+5」や「-3」の形式で入力してください');
            return;
        }
        
        const sign = match[1] === '-' ? -1 : 1;
        const days = parseInt(match[2]) * sign;
        const newDays = currentDays + days;
        
        if (newDays < 0) {
            this.app.showError('有給残日数は0日未満にはできません');
            return;
        }
        
        if (newDays > 50) {
            this.app.showError('有給残日数は50日を超えることはできません');
            return;
        }
        
        this.updateLeaveDays(employeeId, newDays, adjustment);
    }
    
    async updateLeaveDays(employeeId, newDays, adjustment) {
        try {
            this.app.showLoading();
            
            const response = await this.adminService.updateEmployeeLeaveDays(employeeId, {
                remainingLeaveDays: newDays,
                adjustment: adjustment
            });
            
            if (response.success) {
                this.app.showSuccess('有給残日数を更新しました');
                this.loadEmployees();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    showBulkAdjustModal() {
        // 一括調整モーダル（簡易実装）
        this.app.showInfo('一括調整機能は開発中です');
    }
}

// グローバルに公開
window.LeaveManagementComponent = LeaveManagementComponent;
