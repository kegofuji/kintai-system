export function renderAdminPanel() {
    return `
        <div class="admin-panel">
            <h1>管理者パネル</h1>
            
            <div class="admin-sections">
                <div class="section employees">
                    <h2>従業員一覧</h2>
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>名前</th>
                                <th>部署</th>
                                <th>アクション</th>
                            </tr>
                        </thead>
                        <tbody id="employee-list">
                            <!-- 従業員リストが動的に挿入されます -->
                        </tbody>
                    </table>
                </div>
                
                <div class="section pending-requests">
                    <h2>保留中の申請</h2>
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>従業員</th>
                                <th>種類</th>
                                <th>申請日</th>
                                <th>アクション</th>
                            </tr>
                        </thead>
                        <tbody id="pending-request-list">
                            <!-- 保留中の申請リストが動的に挿入されます -->
                        </tbody>
                    </table>
                </div>
                
                <div class="section reports">
                    <h2>レポート生成</h2>
                    <div class="report-actions">
                        <button onclick="generateMonthlyReport()" class="btn btn-primary">月次レポート</button>
                        <button onclick="generateCustomReport()" class="btn btn-secondary">カスタムレポート</button>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// 従業員詳細モーダル
export function showEmployeeDetailsModal(employee) {
    return `
        <div class="modal" id="employee-details-modal">
            <div class="modal-content">
                <h2>従業員詳細</h2>
                <div class="employee-info">
                    <p><strong>ID:</strong> ${employee.id}</p>
                    <p><strong>名前:</strong> ${employee.name}</p>
                    <p><strong>部署:</strong> ${employee.department}</p>
                    <p><strong>入社日:</strong> ${formatDate(employee.joinDate)}</p>
                </div>
                <div class="modal-actions">
                    <button onclick="closeModal()" class="btn btn-secondary">閉じる</button>
                    <button onclick="editEmployee(${employee.id})" class="btn btn-primary">編集</button>
                </div>
            </div>
        </div>
    `;
}

function formatDate(date) {
    return new Date(date).toLocaleDateString('ja-JP');
}
