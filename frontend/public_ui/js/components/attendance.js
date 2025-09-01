export function renderAttendance(records) {
    return `
        <div class="attendance-page">
            <h1>勤怠記録</h1>
            
            <div class="attendance-filters">
                <input type="date" id="start-date" name="start-date">
                <input type="date" id="end-date" name="end-date">
                <button onclick="filterAttendance()" class="btn btn-primary">検索</button>
            </div>
            
            <div class="attendance-actions">
                <button id="check-in-button" class="btn btn-primary">出勤</button>
                <button id="check-out-button" class="btn btn-secondary">退勤</button>
            </div>
            
            <div class="attendance-table">
                <table>
                    <thead>
                        <tr>
                            <th>日付</th>
                            <th>出勤時刻</th>
                            <th>退勤時刻</th>
                            <th>勤務時間</th>
                            <th>状態</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${records.map(record => `
                            <tr>
                                <td>${formatDate(record.date)}</td>
                                <td>${formatTime(record.checkIn)}</td>
                                <td>${formatTime(record.checkOut)}</td>
                                <td>${calculateWorkHours(record.checkIn, record.checkOut)}</td>
                                <td>${record.status}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

function formatDate(date) {
    return new Date(date).toLocaleDateString('ja-JP');
}

function formatTime(time) {
    return time ? new Date(time).toLocaleTimeString('ja-JP') : '-';
}

function calculateWorkHours(checkIn, checkOut) {
    if (!checkIn || !checkOut) return '-';
    
    const start = new Date(checkIn);
    const end = new Date(checkOut);
    const diff = end - start;
    
    const hours = Math.floor(diff / 3600000);
    const minutes = Math.floor((diff % 3600000) / 60000);
    
    return `${hours}時間${minutes}分`;
}
