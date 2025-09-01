export function renderLeaveRequest() {
    return `
        <div class="leave-request-page">
            <h1>休暇申請</h1>
            
            <form id="leave-request-form" class="request-form">
                <div class="form-group">
                    <label for="start-date">開始日</label>
                    <input type="date" id="start-date" name="startDate" required>
                </div>
                
                <div class="form-group">
                    <label for="end-date">終了日</label>
                    <input type="date" id="end-date" name="endDate" required>
                </div>
                
                <div class="form-group">
                    <label for="reason">理由</label>
                    <textarea id="reason" name="reason" required></textarea>
                </div>
                
                <button type="submit" class="btn btn-primary">申請する</button>
            </form>
            
            <div class="request-history">
                <h2>申請履歴</h2>
                <table>
                    <thead>
                        <tr>
                            <th>申請日</th>
                            <th>開始日</th>
                            <th>終了日</th>
                            <th>理由</th>
                            <th>状態</th>
                        </tr>
                    </thead>
                    <tbody id="request-history-table">
                        <!-- 申請履歴が動的に挿入されます -->
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

export function updateRequestHistory(requests) {
    const historyTable = document.querySelector('#request-history-table');
    if (historyTable) {
        historyTable.innerHTML = requests.map(request => `
            <tr>
                <td>${formatDate(request.requestDate)}</td>
                <td>${formatDate(request.startDate)}</td>
                <td>${formatDate(request.endDate)}</td>
                <td>${request.reason}</td>
                <td>${getStatusBadge(request.status)}</td>
            </tr>
        `).join('');
    }
}

function formatDate(date) {
    return new Date(date).toLocaleDateString('ja-JP');
}

function getStatusBadge(status) {
    const statusClasses = {
        'PENDING': 'badge-warning',
        'APPROVED': 'badge-success',
        'REJECTED': 'badge-danger'
    };
    
    return `<span class="badge ${statusClasses[status]}">${translateStatus(status)}</span>`;
}

function translateStatus(status) {
    const statusMap = {
        'PENDING': '審査中',
        'APPROVED': '承認済',
        'REJECTED': '却下'
    };
    
    return statusMap[status] || status;
}
