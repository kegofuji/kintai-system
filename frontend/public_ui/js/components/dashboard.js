// 社員ダッシュボードコンポーネント（設計書画面ID E001完全準拠）
class DashboardComponent {
    constructor(container, app) {
        this.container = container;
        this.app = app;
        this.attendanceService = new AttendanceService();
        this.todayStatus = null;
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
                        <ul class="navbar-nav me-auto" id="nav_menu">
                            <li class="nav-item">
                                <a class="nav-link" href="#/employee/attendance">勤怠履歴</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/employee/leave">有給申請</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/employee/adjustment">打刻修正</a>
                            </li>
                        </ul>
                        <ul class="navbar-nav">
                            <li class="nav-item">
                                <span class="nav-link">こんにちは、${this.app.currentUser.employeeName}さん</span>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#" onclick="app.logout()">ログアウト</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
            
            <div class="dashboard-container">
                <div class="clock-section">
                    <h2>出退勤打刻</h2>
                    <div id="today_status" class="today-status">
                        <p>今日の状況を読み込み中...</p>
                    </div>
                    <div class="clock-buttons">
                        <button type="button" id="clock_in_btn" class="btn btn-clock-in">
                            出勤
                        </button>
                        <button type="button" id="clock_out_btn" class="btn btn-clock-out">
                            退勤
                        </button>
                    </div>
                </div>
            </div>
        `;
    }
    
    async loadTodayStatus() {
        try {
            const today = DateUtil.getCurrentDate();
            const response = await this.attendanceService.getAttendanceHistory({
                employeeId: this.app.currentUser.employeeId,
                dateFrom: today,
                dateTo: today
            });
            
            if (response.success && response.data.attendanceList.length > 0) {
                this.todayStatus = response.data.attendanceList[0];
            }
            
            this.updateStatusDisplay();
            this.updateButtonStates();
            
        } catch (error) {
            this.app.showError('今日の勤怠状況の取得に失敗しました');
        }
    }
    
    updateStatusDisplay() {
        const statusDiv = document.getElementById('today_status');
        
        if (!this.todayStatus) {
            statusDiv.innerHTML = `
                <p><strong>今日（${DateUtil.formatDateJP(DateUtil.getCurrentDate())}）</strong></p>
                <p>まだ出勤打刻されていません</p>
            `;
            return;
        }
        
        const clockIn = this.todayStatus.clockInTime;
        const clockOut = this.todayStatus.clockOutTime;
        
        let statusText = `<p><strong>今日（${DateUtil.formatDateJP(DateUtil.getCurrentDate())}）</strong></p>`;
        
        if (clockIn && clockOut) {
            statusText += `
                <p>出勤時刻: ${clockIn}</p>
                <p>退勤時刻: ${clockOut}</p>
                <p class="text-success">本日の勤務は完了しています</p>
            `;
        } else if (clockIn) {
            statusText += `
                <p>出勤時刻: ${clockIn}</p>
                <p class="text-primary">勤務中です</p>
            `;
        }
        
        statusDiv.innerHTML = statusText;
    }
    
    updateButtonStates() {
        const clockInBtn = document.getElementById('clock_in_btn');
        const clockOutBtn = document.getElementById('clock_out_btn');
        
        if (!this.todayStatus) {
            // 未出勤：出勤ボタンのみ有効
            clockInBtn.disabled = false;
            clockOutBtn.disabled = true;
        } else if (this.todayStatus.clockInTime && !this.todayStatus.clockOutTime) {
            // 出勤済み・未退勤：退勤ボタンのみ有効
            clockInBtn.disabled = true;
            clockOutBtn.disabled = false;
        } else if (this.todayStatus.clockInTime && this.todayStatus.clockOutTime) {
            // 出勤・退勤済み：両方無効
            clockInBtn.disabled = true;
            clockOutBtn.disabled = true;
        }
    }
    
    attachEventListeners() {
        document.getElementById('clock_in_btn').addEventListener('click', async () => {
            await this.handleClockIn();
        });
        
        document.getElementById('clock_out_btn').addEventListener('click', async () => {
            await this.handleClockOut();
        });
    }
    
    async handleClockIn() {
        try {
            this.app.showLoading();
            
            const response = await this.attendanceService.clockIn(this.app.currentUser.employeeId);
            
            if (response.success) {
                this.app.showSuccess(response.data.message || '出勤打刻が完了しました');
                await this.loadTodayStatus();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    async handleClockOut() {
        try {
            this.app.showLoading();
            
            const response = await this.attendanceService.clockOut(this.app.currentUser.employeeId);
            
            if (response.success) {
                this.app.showSuccess(response.data.message || '退勤打刻が完了しました');
                await this.loadTodayStatus();
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
window.DashboardComponent = DashboardComponent;