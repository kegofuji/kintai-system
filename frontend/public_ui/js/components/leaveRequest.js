/**
 * 有給申請コンポーネント（有給申請機能）
 */

class LeaveRequestComponent {
    constructor(containerId) {
        this.container = typeof containerId === 'string' ? 
            document.getElementById(containerId) : containerId;
        this.requestService = new RequestService();
        this.attendanceService = new AttendanceService();
    }
    
    /**
     * 有給申請画面読み込み
     */
    async loadLeaveRequest() {
        if (!this.container) return;
        
        try {
            // 現在の社員情報取得
            const currentUser = window.app?.currentUser;
            if (!currentUser) {
                this.container.innerHTML = '<p>ユーザー情報を取得できませんでした</p>';
                return;
            }
            
            // 申請履歴取得
            const history = await this.requestService.getMyRequestHistory();
            const leaveRequests = history?.success ? history.data?.leaveRequests || [] : [];
            
            const html = `
                <div class="leave-request-container">
                    <h2>有給申請</h2>
                    
                    <!-- 残有給日数表示（設計書通り） -->
                    <div class="leave-balance-section">
                        <div class="balance-card">
                            <h3>残有給日数</h3>
                            <div class="balance-value" id="remaining-leave-days">
                                ${currentUser.paidLeaveRemainingDays || 0}日
                            </div>
                        </div>
                    </div>
                    
                    <!-- 有給申請フォーム（設計書通りの項目） -->
                    <div class="request-form-section">
                        <h3>新規有給申請</h3>
                        <form id="leave-request-form" class="leave-form">
                            <div class="form-group">
                                <label for="leave-date" class="form-label">申請日付 *</label>
                                <input type="date" 
                                       id="leave-date" 
                                       name="leaveDate" 
                                       class="form-control" 
                                       min="${DateUtil.formatDateForInput(new Date(Date.now() + 86400000))}"
                                       required>
                                <div class="error-message" id="leave-date-error"></div>
                                <small class="form-text">※ 明日以降の日付を選択してください</small>
                            </div>
                            
                            <div class="form-group">
                                <label for="leave-reason" class="form-label">理由 *</label>
                                <textarea id="leave-reason" 
                                         name="reason" 
                                         class="form-control" 
                                         rows="3" 
                                         maxlength="200" 
                                         placeholder="有給取得の理由を入力してください（200文字以内）"
                                         required></textarea>
                                <div class="error-message" id="leave-reason-error"></div>
                                <div class="char-counter">
                                    <span id="reason-char-count">0</span>/200文字
                                </div>
                            </div>
                            
                            <div class="form-actions">
                                <button type="submit" class="btn btn-primary">
                                    申請する
                                </button>
                            </div>
                        </form>
                    </div>
                    
                    <!-- 申請履歴（設計書通りの表示） -->
                    <div class="request-history-section">
                        <h3>申請履歴</h3>
                        <div class="history-container">
                            ${this.renderLeaveHistory(leaveRequests)}
                        </div>
                    </div>
                </div>
            `;
            
            this.container.innerHTML = html;
            this.setupEventListeners();
            
        } catch (error) {
            console.error('有給申請画面読み込みエラー:', error);
            this.container.innerHTML = `
                <div class="error-container">
                    <p>画面の読み込みに失敗しました</p>
                    <button onclick="location.reload()" class="btn btn-secondary">再読み込み</button>
                </div>
            `;
        }
    }
    
    /**
     * 有給申請履歴表示
     */
    renderLeaveHistory(requests) {
        if (!requests || requests.length === 0) {
            return '<p class="no-data">申請履歴がありません</p>';
        }
        
        const historyItems = requests.map(request => {
            const statusClass = Formatter.getStatusClass(request.leaveRequestStatus, 'request');
            const statusText = Formatter.formatStatus(request.leaveRequestStatus, 'request');
            
            return `
                <div class="history-item">
                    <div class="history-header">
                        <div class="history-date">
                            ${DateUtil.formatDate(request.leaveRequestDate)}
                        </div>
                        <div class="history-status ${statusClass}">
                            ${statusText}
                        </div>
                    </div>
                    <div class="history-details">
                        <div class="history-reason">
                            <strong>理由:</strong> ${Formatter.escapeHtml(request.leaveRequestReason)}
                        </div>
                        <div class="history-meta">
                            <span>申請日時: ${DateUtil.formatDateTime(request.createdAt)}</span>
                            ${request.approvedAt ? 
                                `<span>承認日時: ${DateUtil.formatDateTime(request.approvedAt)}</span>` : ''}
                        </div>
                    </div>
                </div>
            `;
        }).join('');
        
        return `<div class="history-list">${historyItems}</div>`;
    }
    
    /**
     * イベントリスナー設定
     */
    setupEventListeners() {
        // 有給申請フォーム送信
        const form = document.getElementById('leave-request-form');
        if (form) {
            form.addEventListener('submit', (e) => this.handleLeaveRequestSubmit(e));
        }
        
        // 文字数カウンター
        const reasonTextarea = document.getElementById('leave-reason');
        const charCountElement = document.getElementById('reason-char-count');
        
        if (reasonTextarea && charCountElement) {
            reasonTextarea.addEventListener('input', () => {
                const charCount = reasonTextarea.value.length;
                charCountElement.textContent = charCount;
                
                // 文字数オーバー時の警告
                if (charCount > 200) {
                    charCountElement.style.color = 'var(--danger-color)';
                    reasonTextarea.classList.add('error');
                } else {
                    charCountElement.style.color = 'var(--dark-gray)';
                    reasonTextarea.classList.remove('error');
                }
            });
        }
        
        // 日付選択時の検証
        const leaveDateInput = document.getElementById('leave-date');
        if (leaveDateInput) {
            leaveDateInput.addEventListener('change', () => {
                this.validateLeaveDate(leaveDateInput.value);
            });
        }
    }
    
    /**
     * 有給申請送信処理
     */
    async handleLeaveRequestSubmit(event) {
        event.preventDefault();
        
        try {
            const formData = new FormData(event.target);
            const leaveDate = formData.get('leaveDate');
            const reason = formData.get('reason').trim();
            
            // バリデーション
            if (!this.validateLeaveRequestForm(leaveDate, reason)) {
                return;
            }
            
            // ローディング表示
            const submitBtn = event.target.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            submitBtn.disabled = true;
            submitBtn.textContent = '申請中...';
            
            // 申請送信
            const result = await this.requestService.submitLeaveRequest(
                window.app.currentUser.employeeId,
                leaveDate,
                reason
            );
            
            if (result && result.success) {
                window.app.showMessage(result.message || '有給申請が完了しました', 'success');
                
                // フォームリセット
                event.target.reset();
                document.getElementById('reason-char-count').textContent = '0';
                
                // 画面再読み込み
                await this.loadLeaveRequest();
                
            } else {
                window.app.showMessage(result?.message || '有給申請に失敗しました', 'error');
            }
            
        } catch (error) {
            console.error('有給申請エラー:', error);
            window.app.showMessage(Formatter.formatErrorMessage(error), 'error');
        } finally {
            // ボタン状態復元
            const submitBtn = event.target.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = '申請する';
            }
        }
    }
    
    /**
     * 有給申請フォームバリデーション
     */
    validateLeaveRequestForm(leaveDate, reason) {
        let isValid = true;
        
        // 日付検証
        const dateValidation = this.validateLeaveDate(leaveDate);
        if (!dateValidation) {
            isValid = false;
        }
        
        // 理由検証
        const reasonValidation = Validator.validateReason(reason);
        if (!reasonValidation.valid) {
            this.showFieldError('leave-reason-error', reasonValidation.message);
            document.getElementById('leave-reason').classList.add('error');
            isValid = false;
        } else {
            this.clearFieldError('leave-reason-error');
            document.getElementById('leave-reason').classList.remove('error');
        }
        
        return isValid;
    }
    
    /**
     * 有給申請日検証
     */
    validateLeaveDate(dateString) {
        const dateInput = document.getElementById('leave-date');
        
        // 基本的な日付検証
        const basicValidation = Validator.validateDate(dateString, { futureOnly: true });
        if (!basicValidation.valid) {
            this.showFieldError('leave-date-error', basicValidation.message);
            dateInput.classList.add('error');
            return false;
        }
        
        // 営業日チェック（土日も申請可能なので、この検証は不要）
        // 土日祝日も申請可能
        
        // 重複申請チェックは、サーバー側で行う
        
        this.clearFieldError('leave-date-error');
        dateInput.classList.remove('error');
        return true;
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
window.LeaveRequestComponent = LeaveRequestComponent;