export function renderDashboard(user) {
    return `
        <div class="dashboard">
            <h1>ダッシュボード</h1>
            <div class="welcome-message">
                <h2>ようこそ、${user.name}さん</h2>
            </div>
            
            <div class="quick-actions">
                <button id="check-in-button" class="btn btn-primary">出勤</button>
                <button id="check-out-button" class="btn btn-secondary">退勤</button>
            </div>
            
            <div class="attendance-summary">
                <h3>今月の勤怠サマリー</h3>
                <div id="attendance-stats">
                    <!-- 勤怠統計が動的に挿入されます -->
                </div>
            </div>
            
            <div class="recent-activities">
                <h3>最近の活動</h3>
                <div id="recent-activities-list">
                    <!-- 最近の活動が動的に挿入されます -->
                </div>
            </div>
            
            <div class="notifications">
                <h3>お知らせ</h3>
                <div id="notifications-list">
                    <!-- お知らせが動的に挿入されます -->
                </div>
            </div>
        </div>
    `;
}
