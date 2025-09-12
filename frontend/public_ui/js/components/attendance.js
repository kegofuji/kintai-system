/**
 * 勤怠コンポーネント（設計書通りの画面構成）
 */

class AttendanceComponent {
    constructor(containerId) {
        this.container = typeof containerId === 'string' ? 
            document.getElementById(containerId) : containerId;
        this.attendanceService = new AttendanceService();
        this.requestService = new RequestService();
    }
    
    /**
     * 勤怠履歴画面読み込み（設計書通りのE002画面）
     */
    async loadHistory() {
        if (!this.container) return;
        
        const html = `
            <div class="attendance-history">
                <div class="page-header">
                    <h2>勤怠履歴</h2>
                </div>
                
                <div class="search-section">
                    <div class="search-form">
                        <div class="form-group">
                            <label for="period-select">期間選択</label>
                            <select id="period-select" class="form-control">
                                <option value="current">当月</option>
                                <option value="previous">前月</option>
                                <option value="custom">指定月</option>
                            </select>
                        </div>
                        <div class="form-group" id="custom-month-group" style="display: none;">
                            <label for="custom-month">年月指定</label>
                            <input type="month" id="custom-month" class="form-control">
                        </div>
                        <button type="button" id="search-btn" class="btn btn-primary">検索</button>
                        <button type="button" id="monthly-submit-btn" class="btn btn-warning">月末申請</button>
                    </div>
                </div>
                
                <!-- 勤怠一覧テーブル（設計書通りの項目） -->
                <div class="attendance-table-container">
                    <table class="table table-striped attendance-table" id="attendance-table">
                        <thead>
                            <tr>
                                <th>日付</th>
                                <th>出勤時刻</th>
                                <th>退勤時刻</th>
                                <th>遅刻時間</th>
                                <th>早退時間</th>
                                <th>残業時間</th>
                                <th>深夜時間</th>
                                <th>ステータス</th>
                            </tr>
                        </thead>
                        <tbody id="attendance-tbody">
                            <tr>
                                <td colspan="8" class="text-center">データがありません</td>
                            </tr>
                        </tbody>
                    </table>
                    
                    <!-- スマホ用カード表示 -->
                    <div class="table-card" id="attendance-cards">
                        <p class="text-center">データがありません</p>
                    </div>
                </div>
                
                <!-- 集計情報 -->
                <div class="summary-section" id="attendance-summary" style="display: none;">
                    <h3>月次集計</h3>
                    <div class="summary-grid">
                        <div class="summary-item">
                            <label>実働合計:</label>
                            <span id="total-working">-</span>
                        </div>
                        <div class="summary-item">
                            <label>残業合計:</label>
                            <span id="total-overtime">-</span>
                        </div>
                        <div class="summary-item">
                            <label>深夜合計:</label>
                            <span id="total-night">-</span>
                        </div>
                        <div class="summary-item">
                            <label>遅刻合計:</label>
                            <span id="total-late">-</span>
                        </div>
                        <div class="summary-item">
                            <label>早退合計:</label>
                            <span id="total-early">-</span>
                        </div>
                        <div class="summary-item">
                            <label>有給取得:</label>
                            <span id="paid-leave-days">-</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        this.setupEventListeners();
        await this.loadCurrentMonth();
    }
    
    /**
     * 打刻修正申請画面読み込み（設計書通りのE004画面）
     */
    async loadAdjustmentRequest() {
        if (!this.container) return;
        
        const html = `
            <div class="adjustment-request">
                <div class="page-header">
                    <h2>打刻修正申請</h2>
                </div>
                
                <div class="request-form-section">
                    <form id="adjustment-form" class="adjustment-form">
                        <div class="form-group">
                            <label for="target-date" class="form-label">対象日</label>
                            <input type="date" 
                                   id="target-date" 
                                   name="targetDate" 
                                   class="form-control" 
                                   max="${DateUtil.formatDateForInput(DateUtil.todayInJapan())}"
                                   required>
                            <div class="error-message" id="target-date-error"></div>
                        </div>
                        
                        <div class="time-inputs">
                            <div class="form-group">
                                <label for="corrected-clock-in" class="form-label">修正出勤時刻</label>
                                <input type="time" 
                                       id="corrected-clock-in" 
                                       name="correctedClockInTime" 
                                       class="form-control">
                                <div class="error-message" id="corrected-clock-in-error"></div>
                            </div>
                            
                            <div class="form-group">
                                <label for="corrected-clock-out" class="form-label">修正退勤時刻</label>
                                <input type="time" 
                                       id="corrected-clock-out" 
                                       name="correctedClockOutTime" 
                                       class="form-control">
                                <div class="error-message" id="corrected-clock-out-error"></div>
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label for="adjustment-reason" class="form-label">理由</label>
                            <textarea id="adjustment-reason" 
                                      name="reason" 
                                      class="form-control" 
                                      rows="4" 
                                      maxlength="200" 
                                      placeholder="修正が必要な理由を入力してください（200文字以内）"
                                      required></textarea>
                            <div class="char-count">
                                <span id="reason-count">0</span>/200文字
                            </div>
                            <div class="error-message" id="adjustment-reason-error"></div>
                        </div>
                        
                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary">申請する</button>
                            <button type="button" class="btn btn-secondary" onclick="history.back()">キャンセル</button>
                        </div>
                    </form>
                </div>
                
                <!-- 申請履歴 -->
                <div class="request-history-section">
                    <h3>過去の申請履歴</h3>
                    <div class="history-list" id="adjustment-history">
                        <p class="text-center">読み込み中...</p>
                    </div>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        this.setupAdjustmentEventListeners();
        await this.loadAdjustmentHistory();
    }
    
    /**
     * イベントリスナー設定（勤怠履歴）
     */
    setupEventListeners() {
        // 期間選択変更
        const periodSelect = document.getElementById('period-select');
        const customMonthGroup = document.getElementById('custom-month-group');
        
        if (periodSelect) {
            periodSelect.addEventListener('change', (e) => {
                if (e.target.value === 'custom') {
                    customMonthGroup.style.display = 'block';
                } else {
                    customMonthGroup.style.display = 'none';
                }
            });
        }
        
        // 検索ボタン
        const searchBtn = document.getElementById('search-btn');
        if (searchBtn) {
            searchBtn.addEventListener('click', () => this.searchAttendance());
        }
        
        // 月末申請ボタン
        const monthlySubmitBtn = document.getElementById('monthly-submit-btn');
        if (monthlySubmitBtn) {
            monthlySubmitBtn.addEventListener('click', () => this.submitMonthlyAttendance());
        }
    }
    
    /**
     * イベントリスナー設定（打刻修正申請）
     */
    setupAdjustmentEventListeners() {
        const form = document.getElementById('adjustment-form');
        const reasonTextarea = document.getElementById('adjustment-reason');
        const reasonCount = document.getElementById('reason-count');
        
        // フォーム送信
        if (form) {
            form.addEventListener('submit', (e) => this.handleAdjustmentSubmit(e));
        }
        
        // 文字数カウント
        if (reasonTextarea && reasonCount) {
            reasonTextarea.addEventListener('input', () => {
                reasonCount.textContent = reasonTextarea.value.length;
            });
        }
        
        // リアルタイムバリデーション
        this.setupAdjustmentValidation();
    }
    
    /**
     * 当月データ読み込み
     */
    async loadCurrentMonth() {
        const currentMonth = DateUtil.formatDateForInput(DateUtil.todayInJapan()).substring(0, 7);
        await this.loadAttendanceData(currentMonth);
    }
    
    /**
     * 勤怠データ検索
     */
    async searchAttendance() {
        try {
            const periodSelect = document.getElementById('period-select');
            const customMonth = document.getElementById('custom-month');
            
            let yearMonth;
            const period = periodSelect.value;
            
            if (period === 'current') {
                yearMonth = DateUtil.formatDateForInput(DateUtil.todayInJapan()).substring(0, 7);
            } else if (period === 'previous') {
                const prevMonth = new Date();
                prevMonth.setMonth(prevMonth.getMonth() - 1);
                yearMonth = DateUtil.formatDateForInput(prevMonth).substring(0, 7);
            } else if (period === 'custom') {
                yearMonth = customMonth.value;
            }
            
            if (!yearMonth) {
                window.app.showMessage('年月を選択してください', 'warning');
                return;
            }
            
            await this.loadAttendanceData(yearMonth);
        } catch (error) {
            console.error('勤怠検索エラー:', error);
            window.app.showMessage('勤怠データの検索に失敗しました', 'error');
        }
    }
    
    /**
     * 勤怠データ読み込み
     */
    async loadAttendanceData(yearMonth) {
        try {
            if (!window.app?.currentUser) return;
            
            const result = await this.attendanceService.getAttendanceHistory({
                employeeId: window.app.currentUser.employeeId,
                yearMonth: yearMonth
            });
            
            if (result?.success) {
                this.displayAttendanceData(result.data);
            } else {
                window.app.showMessage(result?.message || '勤怠データの取得に失敗しました', 'error');
            }
        } catch (error) {
            console.error('勤怠データ読み込みエラー:', error);
            window.app.showMessage('勤怠データの読み込み中にエラーが発生しました', 'error');
        }
    }
    
    /**
     * 勤怠データ表示
     */
    displayAttendanceData(data) {
        const tbody = document.getElementById('attendance-tbody');
        const cards = document.getElementById('attendance-cards');
        const summary = document.getElementById('attendance-summary');
        
        if (!data.attendanceList || data.attendanceList.length === 0) {
            if (tbody) {
                tbody.innerHTML = '<tr><td colspan="8" class="text-center">データがありません</td></tr>';
            }
            if (cards) {
                cards.innerHTML = '<p class="text-center">データがありません</p>';
            }
            if (summary) {
                summary.style.display = 'none';
            }
            return;
        }
        
        // テーブル表示
        if (tbody) {
            tbody.innerHTML = data.attendanceList.map(record => {
                const formatted = Formatter.formatAttendanceRecord(record);
                return `
                    <tr>
                        <td>${formatted.date}</td>
                        <td>${formatted.clockIn}</td>
                        <td>${formatted.clockOut}</td>
                        <td class="${record.lateMinutes > 0 ? 'text-danger' : ''}">${formatted.late}</td>
                        <td class="${record.earlyLeaveMinutes > 0 ? 'text-warning' : ''}">${formatted.earlyLeave}</td>
                        <td class="${record.overtimeMinutes > 0 ? 'text-info' : ''}">${formatted.overtime}</td>
                        <td class="${record.nightShiftMinutes > 0 ? 'text-primary' : ''}">${formatted.nightShift}</td>
                        <td><span class="badge ${formatted.statusClass}">${formatted.status}</span></td>
                    </tr>
                `;
            }).join('');
        }
        
        // カード表示（スマホ用）
        if (cards) {
            cards.innerHTML = data.attendanceList.map(record => {
                const formatted = Formatter.formatAttendanceRecord(record);
                return `
                    <div class="card-item">
                        <div class="card-header">${formatted.date}</div>
                        <div class="card-body">
                            <div class="card-row">
                                <span class="card-label">出勤:</span>
                                <span class="card-value">${formatted.clockIn}</span>
                            </div>
                            <div class="card-row">
                                <span class="card-label">退勤:</span>
                                <span class="card-value">${formatted.clockOut}</span>
                            </div>
                            <div class="card-row">
                                <span class="card-label">遅刻:</span>
                                <span class="card-value ${record.lateMinutes > 0 ? 'text-danger' : ''}">${formatted.late}</span>
                            </div>
                            <div class="card-row">
                                <span class="card-label">残業:</span>
                                <span class="card-value ${record.overtimeMinutes > 0 ? 'text-info' : ''}">${formatted.overtime}</span>
                            </div>
                            <div class="card-row">
                                <span class="card-label">ステータス:</span>
                                <span class="card-value"><span class="badge ${formatted.statusClass}">${formatted.status}</span></span>
                            </div>
                        </div>
                    </div>
                `;
            }).join('');
        }
        
        // 集計情報表示
        if (data.summary && summary) {
            const formattedSummary = Formatter.formatAttendanceSummary(data.summary);
            
            document.getElementById('total-working').textContent = formattedSummary.totalWorking;
            document.getElementById('total-overtime').textContent = formattedSummary.totalOvertime;
            document.getElementById('total-night').textContent = formattedSummary.totalNightShift;
            document.getElementById('total-late').textContent = formattedSummary.totalLate;
            document.getElementById('total-early').textContent = formattedSummary.totalEarlyLeave;
            document.getElementById('paid-leave-days').textContent = formattedSummary.paidLeaveDays;
            
            summary.style.display = 'block';
        }
    }
    
    /**
     * 月末申請
     */
    async submitMonthlyAttendance() {
        try {
            const confirmed = await window.app.showConfirm(
                '当月の勤怠を申請しますか？\n申請後は修正できません。'
            );
            
            if (!confirmed) return;
            
            const periodSelect = document.getElementById('period-select');
            const customMonth = document.getElementById('custom-month');
            
            let targetMonth;
            if (periodSelect.value === 'custom') {
                targetMonth = customMonth.value;
            } else {
                targetMonth = DateUtil.formatDateForInput(DateUtil.todayInJapan()).substring(0, 7);
            }
            
            const result = await this.attendanceService.submitMonthlyAttendance(
                window.app.currentUser.employeeId,
                targetMonth
            );
            
            if (result?.success) {
                window.app.showMessage(result.message || '月末申請が完了しました', 'success');
                await this.loadAttendanceData(targetMonth);
            } else {
                window.app.showMessage(result?.message || '月末申請に失敗しました', 'error');
            }
            
        } catch (error) {
            console.error('月末申請エラー:', error);
            window.app.showMessage('月末申請中にエラーが発生しました', 'error');
        }
    }
    
    /**
     * 打刻修正申請フォーム送信
     */
    async handleAdjustmentSubmit(event) {
        event.preventDefault();
        
        try {
            const formData = new FormData(event.target);
            const targetDate = formData.get('targetDate');
            const correctedClockInTime = formData.get('correctedClockInTime');
            const correctedClockOutTime = formData.get('correctedClockOutTime');
            const reason = formData.get('reason');
            
            // バリデーション
            if (!this.validateAdjustmentForm(targetDate, correctedClockInTime, correctedClockOutTime, reason)) {
                return;
            }
            
            const result = await this.requestService.submitAdjustmentRequest(
                window.app.currentUser.employeeId,
                targetDate,
                correctedClockInTime,
                correctedClockOutTime,
                reason
            );
            
            if (result?.success) {
                window.app.showMessage(result.message || '打刻修正申請が完了しました', 'success');
                event.target.reset();
                document.getElementById('reason-count').textContent = '0';
                await this.loadAdjustmentHistory();
            } else {
                window.app.showMessage(result?.message || '打刻修正申請に失敗しました', 'error');
            }
            
        } catch (error) {
            console.error('打刻修正申請エラー:', error);
            window.app.showMessage('申請処理中にエラーが発生しました', 'error');
        }
    }
    
    /**
     * 打刻修正申請バリデーション
     */
    validateAdjustmentForm(targetDate, correctedClockInTime, correctedClockOutTime, reason) {
        let isValid = true;
        
        // 対象日チェック
        const dateResult = Validator.validateDate(targetDate, { pastOnly: true });
        if (!dateResult.valid) {
            this.showFieldError('target-date-error', dateResult.message);
            isValid = false;
        } else {
            this.clearFieldError('target-date-error');
        }
        
        // 時刻チェック（どちらか必須）
        if (!correctedClockInTime && !correctedClockOutTime) {
            this.showFieldError('corrected-clock-in-error', '出勤時刻または退勤時刻のどちらかは必須です');
            isValid = false;
        } else {
            this.clearFieldError('corrected-clock-in-error');
            this.clearFieldError('corrected-clock-out-error');
            
            // 両方入力されている場合の順序チェック
            if (correctedClockInTime && correctedClockOutTime) {
                const inTime = correctedClockInTime.split(':').map(Number);
                const outTime = correctedClockOutTime.split(':').map(Number);
                
                if (inTime[0] * 60 + inTime[1] >= outTime[0] * 60 + outTime[1]) {
                    this.showFieldError('corrected-clock-out-error', '退勤時刻は出勤時刻より後を指定してください');
                    isValid = false;
                }
            }
        }
        
        // 理由チェック
        const reasonResult = Validator.validateReason(reason);
        if (!reasonResult.valid) {
            this.showFieldError('adjustment-reason-error', reasonResult.message);
            isValid = false;
        } else {
            this.clearFieldError('adjustment-reason-error');
        }
        
        return isValid;
    }
    
    /**
     * 打刻修正申請履歴読み込み
     */
    async loadAdjustmentHistory() {
        try {
            const result = await this.requestService.getMyRequestHistory();
            
            if (result?.success && result.data?.adjustmentRequests) {
                this.displayAdjustmentHistory(result.data.adjustmentRequests);
            }
        } catch (error) {
            console.error('申請履歴読み込みエラー:', error);
            const historyContainer = document.getElementById('adjustment-history');
            if (historyContainer) {
                historyContainer.innerHTML = '<p class="text-center text-muted">履歴の読み込みに失敗しました</p>';
            }
        }
    }
    
    /**
     * 打刻修正申請履歴表示
     */
    displayAdjustmentHistory(requests) {
        const container = document.getElementById('adjustment-history');
        if (!container) return;
        
        if (!requests || requests.length === 0) {
            container.innerHTML = '<p class="text-center text-muted">申請履歴がありません</p>';
            return;
        }
        
        container.innerHTML = requests.map(request => `
            <div class="history-item">
                <div class="history-header">
                    <span class="history-date">${DateUtil.formatDate(request.adjustmentTargetDate)}</span>
                    <span class="badge ${Formatter.getStatusClass(request.adjustmentStatus, 'request')}">
                        ${Formatter.formatStatus(request.adjustmentStatus, 'request')}
                    </span>
                </div>
                <div class="history-details">
                    <div class="detail-row">
                        <span class="detail-label">修正出勤時刻:</span>
                        <span class="detail-value">${request.adjustmentRequestedTimeIn ? DateUtil.formatTimeOnly(request.adjustmentRequestedTimeIn) : '-'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">修正退勤時刻:</span>
                        <span class="detail-value">${request.adjustmentRequestedTimeOut ? DateUtil.formatTimeOnly(request.adjustmentRequestedTimeOut) : '-'}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">理由:</span>
                        <span class="detail-value">${request.adjustmentReason}</span>
                    </div>
                    ${request.rejectionReason ? `
                        <div class="detail-row rejection-reason">
                            <span class="detail-label">却下理由:</span>
                            <span class="detail-value">${request.rejectionReason}</span>
                        </div>
                    ` : ''}
                </div>
            </div>
        `).join('');
    }
    
    /**
     * 打刻修正申請バリデーション設定
     */
    setupAdjustmentValidation() {
        const form = document.getElementById('adjustment-form');
        if (!form) return;
        
        const rules = {
            targetDate: {
                label: '対象日',
                required: true,
                validator: (value) => Validator.validateDate(value, { pastOnly: true })
            },
            reason: {
                label: '理由',
                required: true,
                validator: (value) => Validator.validateReason(value)
            }
        };
        
        Validator.setupRealTimeValidation(form, rules);
    }
    
    /**
     * フィールドエラー表示
     */
    showFieldError(elementId, message) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = message;
        }
    }
    
    /**
     * フィールドエラークリア
     */
    clearFieldError(elementId) {
        const errorElement = document.getElementById(elementId);
        if (errorElement) {
            errorElement.textContent = '';
        }
    }
}

// グローバルに公開
window.AttendanceComponent = AttendanceComponent;