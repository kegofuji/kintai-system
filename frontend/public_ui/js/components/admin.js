/**
 * 管理者コンポーネント（管理者機能）
 */

class AdminComponent {
    constructor(containerId) {
        this.container = typeof containerId === 'string' ? 
            document.getElementById(containerId) : containerId;
        this.adminService = new AdminService();
        this.requestService = new RequestService();
        this.attendanceService = new AttendanceService();
    }
    
    /**
     * 管理者ダッシュボード読み込み
     */
    async loadAdminDashboard() {
        if (!this.container) return;
        
        const html = `
            <div class="admin-dashboard-home">
                <h2>管理者ダッシュボード</h2>
                
                <div class="admin-menu-grid">
                    <div class="menu-card" data-nav="employee-management">
                        <h3>社員管理</h3>
                        <p>社員の追加・編集・退職処理</p>
                    </div>
                    
                    <div class="menu-card" data-nav="attendance-management">
                        <h3>勤怠管理</h3>
                        <p>全社員の勤怠データ検索・編集</p>
                    </div>
                    
                    <div class="menu-card" data-nav="request-approval">
                        <h3>申請承認</h3>
                        <p>有給・打刻修正・月末申請の承認</p>
                    </div>
                    
                    <div class="menu-card" data-nav="leave-management">
                        <h3>有給管理</h3>
                        <p>有給承認・付与調整</p>
                    </div>
                    
                    <div class="menu-card" data-nav="report-generation">
                        <h3>レポート出力</h3>
                        <p>月次レポートPDF出力</p>
                    </div>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        this.setupEventListeners();
    }
    
    /**
     * 社員管理画面
     */
    async loadEmployeeManagement() {
        if (!this.container) return;
        
        const html = `
            <div class="employee-management">
                <div class="section-header">
                    <h2>社員管理</h2>
                    <button class="btn btn-primary" id="add-employee-btn">社員追加</button>
                </div>
                
                <div class="search-section">
                    <div class="search-container">
                        <input type="text" id="employee-search" class="form-control" placeholder="社員名・社員コードで検索">
                        <select id="status-filter" class="form-control">
                            <option value="">全て</option>
                            <option value="active">在籍</option>
                            <option value="retired">退職</option>
                        </select>
                        <button class="btn btn-secondary" id="search-employees-btn">検索</button>
                    </div>
                </div>
                
                <div class="employee-list-container">
                    <table class="table" id="employee-table">
                        <thead>
                            <tr>
                                <th>社員コード</th>
                                <th>氏名</th>
                                <th>メール</th>
                                <th>状態</th>
                                <th>有給残日数</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody id="employee-table-body">
                            <!-- データは動的に挿入 -->
                        </tbody>
                    </table>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        this.setupEmployeeManagementEvents();
        await this.loadEmployeeList();
    }
    
    /**
     * 申請承認画面
     */
    async loadRequestApproval() {
        if (!this.container) return;
        
        const html = `
            <div class="request-approval">
                <div class="section-header">
                    <h2>申請承認</h2>
                </div>
                
                <div class="filter-section">
                    <select id="request-type-filter" class="form-control">
                        <option value="">全ての申請</option>
                        <option value="leave">有給申請</option>
                        <option value="adjustment">打刻修正申請</option>
                    </select>
                    <select id="request-status-filter" class="form-control">
                        <option value="">全ての状態</option>
                        <option value="未処理">未処理</option>
                        <option value="承認">承認済</option>
                        <option value="却下">却下</option>
                    </select>
                    <button class="btn btn-secondary" id="filter-requests-btn">絞り込み</button>
                </div>
                
                <div class="request-list-container">
                    <table class="table" id="request-table">
                        <thead>
                            <tr>
                                <th>社員名</th>
                                <th>申請種別</th>
                                <th>申請日付</th>
                                <th>理由</th>
                                <th>状態</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody id="request-table-body">
                            <!-- データは動的に挿入 -->
                        </tbody>
                    </table>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        this.setupRequestApprovalEvents();
        await this.loadRequestList();
    }
    
    /**
     * 勤怠管理画面
     */
    async loadAttendanceManagement() {
        if (!this.container) return;
        
        const html = `
            <div class="attendance-management">
                <div class="section-header">
                    <h2>勤怠管理</h2>
                </div>
                
                <div class="search-section">
                    <div class="search-container">
                        <input type="text" id="attendance-employee-search" class="form-control" placeholder="社員名・社員コード">
                        <input type="date" id="attendance-date-from" class="form-control">
                        <input type="date" id="attendance-date-to" class="form-control">
                        <button class="btn btn-secondary" id="search-attendance-btn">検索</button>
                    </div>
                </div>
                
                <div class="attendance-list-container">
                    <table class="table" id="attendance-table">
                        <thead>
                            <tr>
                                <th>社員名</th>
                                <th>日付</th>
                                <th>出勤時刻</th>
                                <th>退勤時刻</th>
                                <th>遅刻</th>
                                <th>早退</th>
                                <th>残業</th>
                                <th>深夜</th>
                                <th>状態</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody id="attendance-table-body">
                            <!-- データは動的に挿入 -->
                        </tbody>
                    </table>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        this.setupAttendanceManagementEvents();
    }
    
    /**
     * 有給管理画面
     */
    async loadLeaveManagement() {
        if (!this.container) return;
        
        const html = `
            <div class="leave-management">
                <div class="section-header">
                    <h2>有給管理</h2>
                </div>
                
                <div class="leave-adjustment-section">
                    <h3>有給日数調整</h3>
                    <form id="leave-adjustment-form" class="form-inline">
                        <select id="adjustment-employee-select" class="form-control" required>
                            <option value="">社員を選択</option>
                        </select>
                        <input type="number" id="adjustment-days" class="form-control" placeholder="調整日数" min="-99" max="99" required>
                        <input type="text" id="adjustment-reason" class="form-control" placeholder="調整理由" maxlength="200" required>
                        <button type="submit" class="btn btn-primary">調整実行</button>
                    </form>
                </div>
                
                <div class="leave-history-section">
                    <h3>有給調整履歴</h3>
                    <div id="leave-adjustment-history">
                        <!-- 履歴データは動的に表示 -->
                    </div>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        this.setupLeaveManagementEvents();
        await this.loadEmployeeSelect();
    }
    
    /**
     * レポート出力画面
     */
    async loadReportGeneration() {
        if (!this.container) return;
        
        const html = `
            <div class="report-generation">
                <div class="section-header">
                    <h2>レポート出力</h2>
                </div>
                
                <div class="report-form-section">
                    <form id="report-generation-form">
                        <div class="form-group">
                            <label>社員選択</label>
                            <select id="report-employee-select" class="form-control" required>
                                <option value="">社員を選択</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label>対象年月</label>
                            <input type="month" id="report-target-month" class="form-control" required>
                        </div>
                        
                        <button type="submit" class="btn btn-primary">PDF出力</button>
                    </form>
                </div>
                
                <div class="report-status-section" id="report-status" style="display: none;">
                    <h3>レポート生成状況</h3>
                    <div class="status-message" id="report-status-message"></div>
                    <div class="progress" id="report-progress" style="display: none;">
                        <div class="progress-bar"></div>
                    </div>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        this.setupReportGenerationEvents();
        await this.loadEmployeeSelect('report-employee-select');
    }
    
    /**
     * 社員一覧読み込み
     */
    async loadEmployeeList(params = {}) {
        try {
            const result = await this.adminService.getEmployees(params);
            
            if (result?.success && result.data?.employees) {
                this.renderEmployeeTable(result.data.employees);
            } else {
                this.showError('社員一覧の取得に失敗しました');
            }
        } catch (error) {
            console.error('社員一覧取得エラー:', error);
            this.showError('社員一覧の取得中にエラーが発生しました');
        }
    }
    
    /**
     * 申請一覧読み込み
     */
    async loadRequestList(params = {}) {
        try {
            const result = await this.requestService.getRequestList(params);
            
            if (result?.success && result.data) {
                this.renderRequestTable(result.data);
            } else {
                this.showError('申請一覧の取得に失敗しました');
            }
        } catch (error) {
            console.error('申請一覧取得エラー:', error);
            this.showError('申請一覧の取得中にエラーが発生しました');
        }
    }
    
    /**
     * 社員テーブル描画
     */
    renderEmployeeTable(employees) {
        const tbody = document.getElementById('employee-table-body');
        if (!tbody) return;
        
        tbody.innerHTML = employees.map(employee => {
            const formatted = Formatter.formatEmployee(employee);
            return `
                <tr>
                    <td>${formatted.code}</td>
                    <td>${formatted.name}</td>
                    <td>${formatted.email}</td>
                    <td><span class="${formatted.statusClass}">${formatted.status}</span></td>
                    <td>${formatted.paidLeaveRemainingDays}</td>
                    <td>
                        <button class="btn btn-sm btn-secondary edit-employee-btn" data-id="${employee.employeeId}">編集</button>
                        ${employee.employmentStatus === 'active' ? 
                            `<button class="btn btn-sm btn-warning retire-employee-btn" data-id="${employee.employeeId}">退職処理</button>` : 
                            ''
                        }
                    </td>
                </tr>
            `;
        }).join('');
    }
    
    /**
     * 申請テーブル描画
     */
    renderRequestTable(requests) {
        const tbody = document.getElementById('request-table-body');
        if (!tbody) return;
        
        tbody.innerHTML = requests.map(request => {
            const isPending = request.status === '未処理' || request.leaveRequestStatus === '未処理' || request.adjustmentStatus === '未処理';
            const requestType = request.leaveRequestId ? 'leave' : 'adjustment';
            const requestDate = request.leaveRequestDate || request.adjustmentTargetDate;
            const reason = request.leaveRequestReason || request.adjustmentReason;
            const status = request.leaveRequestStatus || request.adjustmentStatus;
            
            return `
                <tr>
                    <td>${request.employee?.employeeName || '不明'}</td>
                    <td>${requestType === 'leave' ? '有給申請' : '打刻修正申請'}</td>
                    <td>${DateUtil.formatDate(requestDate)}</td>
                    <td>${Formatter.truncate(reason, 30)}</td>
                    <td><span class="${Formatter.getStatusClass(status, 'request')}">${status}</span></td>
                    <td>
                        ${isPending ? `
                            <button class="btn btn-sm btn-success approve-request-btn" 
                                    data-id="${request.leaveRequestId || request.adjustmentRequestId}" 
                                    data-type="${requestType}">承認</button>
                            <button class="btn btn-sm btn-danger reject-request-btn" 
                                    data-id="${request.leaveRequestId || request.adjustmentRequestId}" 
                                    data-type="${requestType}">却下</button>
                        ` : '処理済'}
                    </td>
                </tr>
            `;
        }).join('');
    }
    
    /**
     * 社員選択プルダウン読み込み
     */
    async loadEmployeeSelect(selectId = 'adjustment-employee-select') {
        try {
            const result = await this.adminService.getEmployees({ status: 'active' });
            
            if (result?.success && result.data?.employees) {
                const select = document.getElementById(selectId);
                if (select) {
                    select.innerHTML = '<option value="">社員を選択</option>' + 
                        result.data.employees.map(emp => 
                            `<option value="${emp.employeeId}">${emp.employeeName} (${emp.employeeCode})</option>`
                        ).join('');
                }
            }
        } catch (error) {
            console.error('社員選択読み込みエラー:', error);
        }
    }
    
    /**
     * 基本イベントリスナー設定
     */
    setupEventListeners() {
        // メニューカードクリック
        document.querySelectorAll('.menu-card[data-nav]').forEach(card => {
            card.addEventListener('click', (e) => {
                const route = e.currentTarget.dataset.nav;
                if (window.app?.router) {
                    window.app.router.navigate(route);
                }
            });
        });
    }
    
    /**
     * 社員管理イベント設定
     */
    setupEmployeeManagementEvents() {
        // 検索
        const searchBtn = document.getElementById('search-employees-btn');
        if (searchBtn) {
            searchBtn.addEventListener('click', () => {
                const keyword = document.getElementById('employee-search').value;
                const status = document.getElementById('status-filter').value;
                this.loadEmployeeList({ keyword, status });
            });
        }
        
        // 社員追加
        const addBtn = document.getElementById('add-employee-btn');
        if (addBtn) {
            addBtn.addEventListener('click', () => this.showAddEmployeeModal());
        }
        
        // テーブル内ボタンのイベント委譲
        const tableBody = document.getElementById('employee-table-body');
        if (tableBody) {
            tableBody.addEventListener('click', (e) => {
                if (e.target.classList.contains('edit-employee-btn')) {
                    const employeeId = e.target.dataset.id;
                    this.showEditEmployeeModal(employeeId);
                } else if (e.target.classList.contains('retire-employee-btn')) {
                    const employeeId = e.target.dataset.id;
                    this.confirmRetireEmployee(employeeId);
                }
            });
        }
    }
    
    /**
     * 申請承認イベント設定
     */
    setupRequestApprovalEvents() {
        // フィルター
        const filterBtn = document.getElementById('filter-requests-btn');
        if (filterBtn) {
            filterBtn.addEventListener('click', () => {
                const requestType = document.getElementById('request-type-filter').value;
                const status = document.getElementById('request-status-filter').value;
                this.loadRequestList({ requestType, status });
            });
        }
        
        // テーブル内ボタンのイベント委譲
        const tableBody = document.getElementById('request-table-body');
        if (tableBody) {
            tableBody.addEventListener('click', async (e) => {
                if (e.target.classList.contains('approve-request-btn')) {
                    const requestId = e.target.dataset.id;
                    const requestType = e.target.dataset.type;
                    await this.approveRequest(requestId, requestType);
                } else if (e.target.classList.contains('reject-request-btn')) {
                    const requestId = e.target.dataset.id;
                    const requestType = e.target.dataset.type;
                    await this.rejectRequest(requestId, requestType);
                }
            });
        }
    }
    
    /**
     * 申請承認処理
     */
    async approveRequest(requestId, requestType) {
        try {
            if (!window.app?.currentUser?.employeeId) {
                this.showError('承認者情報を取得できません');
                return;
            }
            
            const confirmed = await this.showConfirm('この申請を承認しますか？');
            if (!confirmed) return;
            
            const result = await this.requestService.approveRequest(
                requestId, 
                requestType, 
                window.app.currentUser.employeeId
            );
            
            if (result?.success) {
                this.showSuccess('申請を承認しました');
                await this.loadRequestList();
            } else {
                this.showError(result?.message || '承認に失敗しました');
            }
        } catch (error) {
            console.error('承認エラー:', error);
            this.showError('承認処理中にエラーが発生しました');
        }
    }
    
    /**
     * 申請却下処理
     */
    async rejectRequest(requestId, requestType) {
        try {
            const reason = prompt('却下理由を入力してください：');
            if (!reason) return;
            
            if (!window.app?.currentUser?.employeeId) {
                this.showError('承認者情報を取得できません');
                return;
            }
            
            const result = await this.requestService.rejectRequest(
                requestId, 
                requestType, 
                window.app.currentUser.employeeId,
                reason
            );
            
            if (result?.success) {
                this.showSuccess('申請を却下しました');
                await this.loadRequestList();
            } else {
                this.showError(result?.message || '却下に失敗しました');
            }
        } catch (error) {
            console.error('却下エラー:', error);
            this.showError('却下処理中にエラーが発生しました');
        }
    }
    
    /**
     * レポート生成イベント設定
     */
    setupReportGenerationEvents() {
        const form = document.getElementById('report-generation-form');
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.generateReport();
            });
        }
    }
    
    /**
     * レポート生成処理
     */
    async generateReport() {
        try {
            const employeeId = document.getElementById('report-employee-select').value;
            const yearMonth = document.getElementById('report-target-month').value;
            
            if (!employeeId || !yearMonth) {
                this.showError('社員と対象年月を選択してください');
                return;
            }
            
            // ステータス表示
            this.showReportStatus('レポートを生成中...', true);
            
            const result = await this.adminService.generateReport(employeeId, yearMonth);
            
            if (result?.success && result.data?.pdf_url) {
                this.showReportStatus('レポートが生成されました', false);
                
                // ダウンロードリンク表示
                const downloadLink = document.createElement('a');
                downloadLink.href = result.data.pdf_url;
                downloadLink.download = result.data.filename || 'attendance_report.pdf';
                downloadLink.textContent = 'PDFをダウンロード';
                downloadLink.className = 'btn btn-success';
                
                const statusDiv = document.getElementById('report-status-message');
                statusDiv.appendChild(document.createElement('br'));
                statusDiv.appendChild(downloadLink);
                
                // 自動ダウンロード
                downloadLink.click();
            } else {
                this.showReportStatus('レポート生成に失敗しました', false);
            }
        } catch (error) {
            console.error('レポート生成エラー:', error);
            this.showReportStatus('レポート生成中にエラーが発生しました', false);
        }
    }
    
    /**
     * レポート生成ステータス表示
     */
    showReportStatus(message, showProgress = false) {
        const statusSection = document.getElementById('report-status');
        const statusMessage = document.getElementById('report-status-message');
        const progress = document.getElementById('report-progress');
        
        if (statusSection) statusSection.style.display = 'block';
        if (statusMessage) statusMessage.textContent = message;
        if (progress) progress.style.display = showProgress ? 'block' : 'none';
    }
    
    /**
     * 有給管理イベント設定
     */
    setupLeaveManagementEvents() {
        const form = document.getElementById('leave-adjustment-form');
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.adjustPaidLeave();
            });
        }
    }
    
    /**
     * 有給日数調整処理
     */
    async adjustPaidLeave() {
        try {
            const employeeId = document.getElementById('adjustment-employee-select').value;
            const adjustmentDays = parseInt(document.getElementById('adjustment-days').value);
            const reason = document.getElementById('adjustment-reason').value;
            
            if (!employeeId || isNaN(adjustmentDays) || !reason) {
                this.showError('全ての項目を入力してください');
                return;
            }
            
            const confirmed = await this.showConfirm(
                `有給日数を${adjustmentDays}日調整しますか？`
            );
            if (!confirmed) return;
            
            const result = await this.adminService.adjustPaidLeave(employeeId, adjustmentDays, reason);
            
            if (result?.success) {
                this.showSuccess('有給日数を調整しました');
                document.getElementById('leave-adjustment-form').reset();
            } else {
                this.showError(result?.message || '有給日数調整に失敗しました');
            }
        } catch (error) {
            console.error('有給調整エラー:', error);
            this.showError('有給日数調整中にエラーが発生しました');
        }
    }
    
    /**
     * 勤怠管理イベント設定
     */
    setupAttendanceManagementEvents() {
        const searchBtn = document.getElementById('search-attendance-btn');
        if (searchBtn) {
            searchBtn.addEventListener('click', () => {
                // 勤怠データ検索処理
                this.searchAttendanceData();
            });
        }
    }
    
    /**
     * 勤怠データ検索
     */
    async searchAttendanceData() {
        // 実装省略（基本的な検索機能）
        this.showInfo('勤怠データ検索機能は実装中です');
    }
    
    /**
     * メッセージ表示ヘルパー
     */
    showSuccess(message) {
        if (window.app) {
            window.app.showMessage(message, 'success');
        }
    }
    
    showError(message) {
        if (window.app) {
            window.app.showMessage(message, 'error');
        }
    }
    
    showInfo(message) {
        if (window.app) {
            window.app.showMessage(message, 'info');
        }
    }
    
    async showConfirm(message) {
        if (window.app) {
            return await window.app.showConfirm(message);
        }
        return confirm(message);
    }
}

// グローバルに公開
window.AdminComponent = AdminComponent;