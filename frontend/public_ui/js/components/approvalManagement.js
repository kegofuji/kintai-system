// 申請承認画面コンポーネント（設計書画面ID A004完全準拠）
class ApprovalManagementComponent {
    constructor(container, app) {
        this.container = container;
        this.app = app;
        this.requestService = new RequestService();
        this.pendingRequests = [];
    }
    
    render() {
        return `
            <nav class="navbar navbar-expand-lg navbar-custom">
                <div class="container-fluid">
                    <span class="navbar-brand">勤怠管理システム - 管理者</span>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarNav">
                        <ul class="navbar-nav me-auto">
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/dashboard">ダッシュボード</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/employees">社員管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/attendance">勤怠管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="#/admin/approvals">申請承認</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/leave-management">有給管理</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#/admin/reports">レポート出力</a>
                            </li>
                        </ul>
                        <ul class="navbar-nav">
                            <li class="nav-item">
                                <span class="nav-link">${this.app.currentUser.employeeName}さん（管理者）</span>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#" onclick="app.logout()">ログアウト</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
            
            <div class="admin-dashboard">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-12">
                            <h1 class="mt-4 mb-4">申請承認</h1>
                        </div>
                    </div>
                    
                    <div class="approval-management">
                        <div class="filter-section mb-3">
                            <div class="row align-items-end">
                                <div class="col-md-3">
                                    <label for="request_type_filter" class="form-label">申請種別</label>
                                    <select id="request_type_filter" class="form-select">
                                        <option value="">全て</option>
                                        <option value="leave">有給申請</option>
                                        <option value="adjustment">打刻修正</option>
                                        <option value="monthly">月末申請</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label for="status_filter" class="form-label">ステータス</label>
                                    <select id="status_filter" class="form-select">
                                        <option value="">全て</option>
                                        <option value="pending">未処理</option>
                                        <option value="approved">承認済</option>
                                        <option value="rejected">却下済</option>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <button type="button" id="filter_btn" class="btn btn-primary">
                                        フィルター
                                    </button>
                                </div>
                            </div>
                        </div>
                        
                        <div class="table-responsive">
                            <table class="table table-hover" id="approval_table">
                                <thead>
                                    <tr>
                                        <th>申請日</th>
                                        <th>申請者</th>
                                        <th>申請種別</th>
                                        <th>申請内容</th>
                                        <th>ステータス</th>
                                        <th>操作</th>
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
        
        this.loadPendingRequests();
    }
    
    attachEventListeners() {
        const filterBtn = document.getElementById('filter_btn');
        
        filterBtn.addEventListener('click', () => {
            this.loadPendingRequests();
        });
    }
    
    async loadPendingRequests() {
        try {
            this.app.showLoading();
            
            const requestType = document.getElementById('request_type_filter').value;
            const status = document.getElementById('status_filter').value;
            
            const params = {};
            if (requestType) params.requestType = requestType;
            if (status) params.status = status;
            
            const response = await this.requestService.getPendingRequests(params);
            
            if (response.success) {
                this.pendingRequests = response.data;
                this.renderApprovalTable(this.pendingRequests);
            } else {
                this.app.showError('申請データの取得に失敗しました');
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    renderApprovalTable(requests) {
        const tbody = document.querySelector('#approval_table tbody');
        
        if (requests.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted">該当する申請がありません</td>
                </tr>
            `;
            return;
        }
        
        tbody.innerHTML = requests.map(request => `
            <tr>
                <td>${DateUtil.formatDateJP(request.requestDate)}</td>
                <td>${request.employeeName}</td>
                <td>
                    <span class="type-${request.requestType}">
                        ${Formatter.formatRequestType(request.requestType)}
                    </span>
                </td>
                <td>${this.getRequestContent(request)}</td>
                <td>
                    <span class="status-${request.status === '承認' ? 'approved' : 
                                        request.status === '却下' ? 'rejected' : 'pending'}">
                        ${Formatter.formatRequestStatus(request.status)}
                    </span>
                </td>
                <td>
                    ${request.status === '未処理' ? 
                        `<div class="btn-group" role="group">
                            <button class="btn btn-sm btn-success" onclick="this.approveRequest(${request.requestId}, '${request.requestType}')">
                                承認
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="this.rejectRequest(${request.requestId}, '${request.requestType}')">
                                却下
                            </button>
                            <button class="btn btn-sm btn-outline-primary" onclick="this.viewRequestDetail(${request.requestId}, '${request.requestType}')">
                                詳細
                            </button>
                        </div>` : 
                        `<button class="btn btn-sm btn-outline-primary" onclick="this.viewRequestDetail(${request.requestId}, '${request.requestType}')">
                            詳細
                        </button>`
                    }
                </td>
            </tr>
        `).join('');
    }
    
    getRequestContent(request) {
        switch (request.requestType) {
            case 'leave':
                return `有給取得日: ${DateUtil.formatDateJP(request.leaveDate)}`;
            case 'adjustment':
                return `修正日: ${DateUtil.formatDateJP(request.adjustmentDate)}`;
            case 'monthly':
                return `対象月: ${request.yearMonth}`;
            default:
                return '-';
        }
    }
    
    async approveRequest(requestId, requestType) {
        const comment = prompt('承認コメント（任意）:');
        if (comment === null) return; // キャンセル
        
        try {
            this.app.showLoading();
            
            const response = await this.requestService.approveRequest(requestId, requestType, comment);
            
            if (response.success) {
                this.app.showSuccess('申請を承認しました');
                this.loadPendingRequests();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    async rejectRequest(requestId, requestType) {
        const comment = prompt('却下理由（必須）:');
        if (!comment || comment.trim() === '') {
            this.app.showError('却下理由を入力してください');
            return;
        }
        
        try {
            this.app.showLoading();
            
            const response = await this.requestService.rejectRequest(requestId, requestType, comment);
            
            if (response.success) {
                this.app.showSuccess('申請を却下しました');
                this.loadPendingRequests();
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    async viewRequestDetail(requestId, requestType) {
        try {
            this.app.showLoading();
            
            const response = await this.requestService.getRequestDetail(requestId, requestType);
            
            if (response.success) {
                this.showRequestDetailModal(response.data);
            } else {
                this.app.showError('申請詳細の取得に失敗しました');
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
    
    showRequestDetailModal(request) {
        const modalHtml = `
            <div class="modal fade" id="requestDetailModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">申請詳細</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <table class="table table-sm">
                                        <tbody>
                                            <tr><th>申請者</th><td>${request.employeeName}</td></tr>
                                            <tr><th>申請日</th><td>${DateUtil.formatDateJP(request.requestDate)}</td></tr>
                                            <tr><th>申請種別</th><td>${Formatter.formatRequestType(request.requestType)}</td></tr>
                                            <tr><th>ステータス</th><td>${Formatter.formatRequestStatus(request.status)}</td></tr>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="col-md-6">
                                    <table class="table table-sm">
                                        <tbody>
                                            ${this.getRequestDetailRows(request)}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-12">
                                    <h6>申請理由</h6>
                                    <p class="border p-2">${request.reason || '-'}</p>
                                </div>
                            </div>
                            ${request.approverComment ? `
                                <div class="row">
                                    <div class="col-12">
                                        <h6>承認者コメント</h6>
                                        <p class="border p-2">${request.approverComment}</p>
                                    </div>
                                </div>
                            ` : ''}
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">閉じる</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        
        const modal = new bootstrap.Modal(document.getElementById('requestDetailModal'));
        modal.show();
        
        document.getElementById('requestDetailModal').addEventListener('hidden.bs.modal', () => {
            document.getElementById('requestDetailModal').remove();
        });
    }
    
    getRequestDetailRows(request) {
        switch (request.requestType) {
            case 'leave':
                return `
                    <tr><th>有給取得日</th><td>${DateUtil.formatDateJP(request.leaveDate)}</td></tr>
                `;
            case 'adjustment':
                return `
                    <tr><th>修正対象日</th><td>${DateUtil.formatDateJP(request.adjustmentDate)}</td></tr>
                    <tr><th>出勤時刻</th><td>${request.clockInTime || '-'}</td></tr>
                    <tr><th>退勤時刻</th><td>${request.clockOutTime || '-'}</td></tr>
                `;
            case 'monthly':
                return `
                    <tr><th>対象月</th><td>${request.yearMonth}</td></tr>
                `;
            default:
                return '';
        }
    }
}

// グローバルに公開
window.ApprovalManagementComponent = ApprovalManagementComponent;
