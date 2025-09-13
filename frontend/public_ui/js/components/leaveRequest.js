// 有給申請画面コンポーネント（設計書画面ID E003完全準拠）
class LeaveRequestComponent {
    constructor(container, app) {
        this.container = container;
        this.app = app;
        this.requestService = new RequestService();
        this.remainingDays = 0;
    }
    
    render() {
        return `
            <nav class="navbar navbar-expand-lg navbar-custom">
                <div class="container-fluid">
                    <span class="navbar-brand">勤怠管理システム</span>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarNav">
                        <ul class="navbar-nav me-auto">
                            <li class="nav-item">
                                <a class="nav-link" href="#/employee/dashboard">ダッシュボード</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/employee/attendance">勤怠履歴</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="#/employee/leave">有給申請</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/employee/adjustment">打刻修正</a>
                            </li>
                        </ul>
                        <ul class="navbar-nav">
                            <li class="nav-item">
                                <span class="nav-link">${this.app.currentUser.employeeName}さん</span>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#" onclick="app.logout()">ログアウト</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
            
            <div class="dashboard-container">
                <div class="request-form">
                    <h2>有給申請</h2>
                    
                    <div class="remaining-days" id="remaining_days">
                        残有給日数: 読み込み中...
                    </div>
                    
                    <form id="leaveRequestForm">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="form-floating mb-3">
                                    <input type="date" class="form-control" id="request_date" 
                                           min="${DateUtil.getRelativeDate(1)}" required>
                                    <label for="request_date">申請日付</label>
                                    <div class="invalid-feedback"></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-floating mb-3">
                                    <textarea class="form-control" id="reason" 
                                              placeholder="理由" style="height: 100px;" 
                                              maxlength="200" required></textarea>
                                    <label for="reason">理由</label>
                                    <div class="invalid-feedback"></div>
                                    <div class="form-text">
                                        <span id="reason_counter">0</span>/200文字
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="text-center">
                            <button type="submit" id="submit_btn" class="btn btn-success">
                                申請
                            </button>
                        </div>
                    </form>
                    
                    <div class="form-section mt-5">
                        <h3>申請履歴</h3>
                        <div class="table-responsive">
                            <table class="table table-hover" id="request_history">
                                <thead>
                                    <tr>
                                        <th>申請日</th>
                                        <th>取得予定日</th>
                                        <th>理由</th>
                                        <th>状態</th>
                                        <th>申請日時</th>
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
        
        this.loadRemainingDays();
        this.loadRequestHistory();
    }
    
    attachEventListeners() {
        const form = document.getElementById('leaveRequestForm');
        const reasonTextarea = document.getElementById('reason');
        const reasonCounter = document.getElementById('reason_counter');
        const dateInput = document.getElementById('request_date');
        
        // 文字数カウンター
        reasonTextarea.addEventListener('input', (e) => {
            reasonCounter.textContent = e.target.value.length;
            this.validateReason(e.target.value);
        });
        
        // 日付バリデーション
        dateInput.addEventListener('change', (e) => {
            this.validateLeaveDate(e.target.value);
        });
        
        // フォーム送信
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.handleSubmit();
        });
    }
    
    async loadRemainingDays() {
        try {
            // 社員情報から有給残日数取得（実装は簡略化）
            // 実際のシステムでは専用APIが必要
            this.remainingDays = 10; // 仮の値
            
            document.getElementById('remaining_days').innerHTML = 
                `残有給日数: <strong>${this.remainingDays}日</strong>`;
            
        } catch (error) {
            this.app.showError('有給残日数の取得に失敗しました');
        }
    }
    
    async loadRequestHistory() {
        try {
            const response = await this.requestService.getRequestList({
                requestType: 'leave',
                employeeId: this.app.currentUser.employeeId
            });
            
            if (response.success) {
                this.renderRequestHistory(response.data);
            }
            
        } catch (error) {
            console.error('申請履歴の取得に失敗しました:', error);
        }
    }
    
    renderRequestHistory(requests) {
        const tbody = document.querySelector('#request_history tbody');
        
        if (!requests || requests.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted">申請履歴がありません</td>
                </tr>
            `;
            return;
        }
        
        // 有給申請のみフィルタリング
        const leaveRequests = requests.filter(req => req.requestType === 'leave' || req.leaveRequestId);
        
        tbody.innerHTML = leaveRequests.map(request => `
            <tr>
                <td>${DateUtil.formatDateJP(request.createdAt || request.leaveRequestDate)}</td>
                <td>${DateUtil.formatDateJP(request.leaveRequestDate || request.requestDate)}</td>
                <td>${request.leaveRequestReason || request.reason}</td>
                <td>
                    <span class="request-${(request.leaveRequestStatus || request.status).toLowerCase()}">
                        ${Formatter.formatRequestStatus(request.leaveRequestStatus || request.status)}
                    </span>
                </td>
                <td>${new Date(request.createdAt).toLocaleString('ja-JP')}</td>
            </tr>
        `).join('');
    }
    
    validateLeaveDate(value) {
        const input = document.getElementById('request_date');
        const feedback = input.nextElementSibling.nextElementSibling;
        
        const result = Validator.validateLeaveDate(value);
        
        if (result.valid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            feedback.textContent = '';
        } else {
            input.classList.remove('is-valid');
            input.classList.add('is-invalid');
            feedback.textContent = result.message;
        }
        
        return result.valid;
    }
    
    validateReason(value) {
        const input = document.getElementById('reason');
        const feedback = input.nextElementSibling.nextElementSibling;
        
        const result = Validator.validateReason(value);
        
        if (result.valid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            feedback.textContent = '';
        } else {
            input.classList.remove('is-valid');
            input.classList.add('is-invalid');
            feedback.textContent = result.message;
        }
        
        return result.valid;
    }
    
    async handleSubmit() {
        const requestDate = document.getElementById('request_date').value;
        const reason = document.getElementById('reason').value.trim();
        
        // バリデーション
        const isValidDate = this.validateLeaveDate(requestDate);
        const isValidReason = this.validateReason(reason);
        
        if (!isValidDate || !isValidReason) {
            return;
        }
        
        // 残日数チェック
        if (this.remainingDays <= 0) {
            this.app.showError('有給残日数が不足しています');
            return;
        }
        
        try {
            this.app.showLoading();
            
            const response = await this.requestService.submitLeaveRequest({
                employeeId: this.app.currentUser.employeeId,
                leaveDate: requestDate,
                reason: reason
            });
            
            if (response.success) {
                this.app.showSuccess(`有給申請が完了しました（残り${response.data.remainingDays || this.remainingDays - 1}日）`);
                document.getElementById('leaveRequestForm').reset();
                this.loadRemainingDays();
                this.loadRequestHistory();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
}

// グローバルに公開
window.LeaveRequestComponent = LeaveRequestComponent;