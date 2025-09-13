// 打刻修正申請画面コンポーネント（設計書画面ID E004完全準拠）
class AdjustmentRequestComponent {
    constructor(container, app) {
        this.container = container;
        this.app = app;
        this.requestService = new RequestService();
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
                                <a class="nav-link" href="#/employee/leave">有給申請</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="#/employee/adjustment">打刻修正</a>
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
                    <h2>打刻修正申請</h2>
                    
                    <form id="adjustmentRequestForm">
                        <div class="row">
                            <div class="col-md-4">
                                <div class="form-floating mb-3">
                                    <input type="date" class="form-control" id="target_date" 
                                           max="${DateUtil.getCurrentDate()}" required>
                                    <label for="target_date">対象日</label>
                                    <div class="invalid-feedback"></div>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-floating mb-3">
                                    <input type="time" class="form-control" id="corrected_clock_in">
                                    <label for="corrected_clock_in">修正出勤時刻</label>
                                    <div class="invalid-feedback"></div>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-floating mb-3">
                                    <input type="time" class="form-control" id="corrected_clock_out">
                                    <label for="corrected_clock_out">修正退勤時刻</label>
                                    <div class="invalid-feedback"></div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-12">
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
                            <button type="submit" id="submit_btn" class="btn btn-warning">
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
                                        <th>対象日</th>
                                        <th>修正出勤時刻</th>
                                        <th>修正退勤時刻</th>
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
        
        this.loadRequestHistory();
    }
    
    attachEventListeners() {
        const form = document.getElementById('adjustmentRequestForm');
        const reasonTextarea = document.getElementById('reason');
        const reasonCounter = document.getElementById('reason_counter');
        const targetDateInput = document.getElementById('target_date');
        const clockInInput = document.getElementById('corrected_clock_in');
        const clockOutInput = document.getElementById('corrected_clock_out');
        
        // 文字数カウンター
        reasonTextarea.addEventListener('input', (e) => {
            reasonCounter.textContent = e.target.value.length;
            this.validateReason(e.target.value);
        });
        
        // 日付バリデーション
        targetDateInput.addEventListener('change', (e) => {
            this.validateAdjustmentDate(e.target.value);
        });
        
        // 時刻バリデーション
        clockInInput.addEventListener('blur', (e) => {
            if (e.target.value) this.validateTime(e.target.value, 'corrected_clock_in');
        });
        
        clockOutInput.addEventListener('blur', (e) => {
            if (e.target.value) this.validateTime(e.target.value, 'corrected_clock_out');
        });
        
        // フォーム送信
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.handleSubmit();
        });
    }
    
    async loadRequestHistory() {
        try {
            const response = await this.requestService.getRequestList({
                requestType: 'adjustment',
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
                    <td colspan="6" class="text-center text-muted">申請履歴がありません</td>
                </tr>
            `;
            return;
        }
        
        // 打刻修正申請のみフィルタリング
        const adjustmentRequests = requests.filter(req => req.requestType === 'adjustment' || req.adjustmentRequestId);
        
        tbody.innerHTML = adjustmentRequests.map(request => `
            <tr>
                <td>${DateUtil.formatDateJP(request.adjustmentTargetDate || request.targetDate)}</td>
                <td>${request.adjustmentRequestedTimeIn ? DateUtil.formatTime(request.adjustmentRequestedTimeIn) : 
                     request.correctedClockInTime || '-'}</td>
                <td>${request.adjustmentRequestedTimeOut ? DateUtil.formatTime(request.adjustmentRequestedTimeOut) : 
                     request.correctedClockOutTime || '-'}</td>
                <td>${request.adjustmentReason || request.reason}</td>
                <td>
                    <span class="request-${(request.adjustmentStatus || request.status).toLowerCase()}">
                        ${Formatter.formatRequestStatus(request.adjustmentStatus || request.status)}
                    </span>
                </td>
                <td>${new Date(request.createdAt).toLocaleString('ja-JP')}</td>
            </tr>
        `).join('');
    }
    
    validateAdjustmentDate(value) {
        const input = document.getElementById('target_date');
        const feedback = input.nextElementSibling.nextElementSibling;
        
        const result = Validator.validateAdjustmentDate(value);
        
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
    
    validateTime(value, inputId) {
        const input = document.getElementById(inputId);
        const feedback = input.nextElementSibling.nextElementSibling;
        
        const result = Validator.validateTime(value);
        
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
        const targetDate = document.getElementById('target_date').value;
        const clockInTime = document.getElementById('corrected_clock_in').value;
        const clockOutTime = document.getElementById('corrected_clock_out').value;
        const reason = document.getElementById('reason').value.trim();
        
        // バリデーション
        const isValidDate = this.validateAdjustmentDate(targetDate);
        const isValidReason = this.validateReason(reason);
        
        // どちらか一方の時刻は必須（設計書仕様）
        if (!clockInTime && !clockOutTime) {
            this.app.showError('出勤時刻または退勤時刻のどちらか一方は入力してください');
            return;
        }
        
        let isValidClockIn = true;
        let isValidClockOut = true;
        
        if (clockInTime) {
            isValidClockIn = this.validateTime(clockInTime, 'corrected_clock_in');
        }
        if (clockOutTime) {
            isValidClockOut = this.validateTime(clockOutTime, 'corrected_clock_out');
        }
        
        if (!isValidDate || !isValidReason || !isValidClockIn || !isValidClockOut) {
            return;
        }
        
        // 退勤時刻 > 出勤時刻チェック
        if (clockInTime && clockOutTime && clockOutTime <= clockInTime) {
            this.app.showError('退勤時刻は出勤時刻より後の時間を入力してください');
            return;
        }
        
        try {
            this.app.showLoading();
            
            const response = await this.requestService.submitAdjustmentRequest({
                employeeId: this.app.currentUser.employeeId,
                targetDate: targetDate,
                correctedClockInTime: clockInTime || null,
                correctedClockOutTime: clockOutTime || null,
                reason: reason
            });
            
            if (response.success) {
                this.app.showSuccess('打刻修正申請が完了しました');
                document.getElementById('adjustmentRequestForm').reset();
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
window.AdjustmentRequestComponent = AdjustmentRequestComponent;