/**
 * ダッシュボードコンポーネント（社員用）
 */

class Dashboard {
    constructor(containerId) {
        this.container = typeof containerId === 'string' ? 
            document.getElementById(containerId) : containerId;
        this.attendanceService = new AttendanceService();
    }
    
    async loadEmployeeDashboard() {
        if (!this.container) return;
        
        const html = `
            <div class="employee-dashboard">
                <h2>社員ダッシュボード</h2>
                <div class="dashboard-content">
                    <div class="quick-stats">
                        <div class="stat-card">
                            <h3>本月勤務日数</h3>
                            <div class="stat-value" id="monthly-work-days">-</div>
                        </div>
                        <div class="stat-card">
                            <h3>残業時間</h3>
                            <div class="stat-value" id="monthly-overtime">-</div>
                        </div>
                        <div class="stat-card">
                            <h3>有給残日数</h3>
                            <div class="stat-value" id="remaining-leave">-</div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        this.container.innerHTML = html;
        await this.loadQuickStats();
    }
    
    async loadQuickStats() {
        try {
            if (!window.app?.currentUser) return;
            
            const currentMonth = DateUtil.formatDateForInput(DateUtil.todayInJapan()).substring(0, 7);
            const history = await this.attendanceService.getAttendanceHistory({
                employeeId: window.app.currentUser.employeeId,
                yearMonth: currentMonth
            });
            
            if (history?.success && history.data) {
                const summary = history.data.summary;
                const workDays = history.data.attendanceList?.length || 0;
                
                document.getElementById('monthly-work-days').textContent = `${workDays}日`;
                document.getElementById('monthly-overtime').textContent = 
                    Formatter.formatMinutesToHHMM(summary?.totalOvertimeMinutes || 0);
            }
        } catch (error) {
            console.error('Quick stats loading error:', error);
        }
    }
}

window.Dashboard = Dashboard;