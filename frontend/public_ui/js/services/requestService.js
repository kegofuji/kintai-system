/**
 * 申請サービス（有給・打刻修正申請管理）
 */

class RequestService {
    constructor() {
        this.baseUrl = CONFIG.API_BASE_URL;
    }
    
    /**
     * 有給申請
     */
    async submitLeaveRequest(employeeId, leaveDate, reason) {
        try {
            const response = await fetch(`${this.baseUrl}/requests/leave`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    employeeId,
                    leaveDate,
                    reason
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '有給申請に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('有給申請エラー:', error);
            throw error;
        }
    }
    
    /**
     * 打刻修正申請
     */
    async submitAdjustmentRequest(employeeId, targetDate, correctedClockInTime, correctedClockOutTime, reason) {
        try {
            const response = await fetch(`${this.baseUrl}/requests/adjustment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    employeeId,
                    targetDate,
                    correctedClockInTime,
                    correctedClockOutTime,
                    reason
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '打刻修正申請に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('打刻修正申請エラー:', error);
            throw error;
        }
    }
    
    /**
     * 申請履歴取得
     */
    async getMyRequestHistory() {
        try {
            const response = await fetch(`${this.baseUrl}/requests/my-history`, {
                method: 'GET',
                credentials: 'include'
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '申請履歴の取得に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('申請履歴取得エラー:', error);
            throw error;
        }
    }
    
    /**
     * 申請一覧取得（管理者用）
     */
    async getRequestList(params = {}) {
        try {
            const queryParams = new URLSearchParams();
            
            if (params.requestType) {
                queryParams.append('requestType', params.requestType);
            }
            if (params.status) {
                queryParams.append('status', params.status);
            }
            if (params.employeeId) {
                queryParams.append('employeeId', params.employeeId);
            }
            if (params.employeeName) {
                queryParams.append('employeeName', params.employeeName);
            }
            
            const response = await fetch(`${this.baseUrl}/requests/list?${queryParams}`, {
                method: 'GET',
                credentials: 'include'
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '申請一覧の取得に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('申請一覧取得エラー:', error);
            throw error;
        }
    }
    
    /**
     * 申請承認
     */
    async approveRequest(requestId, requestType, approverId, comment = '') {
        try {
            const response = await fetch(`${this.baseUrl}/requests/approve/${requestId}?requestType=${requestType}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    approverId,
                    comment
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '申請の承認に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('申請承認エラー:', error);
            throw error;
        }
    }
    
    /**
     * 申請却下
     */
    async rejectRequest(requestId, requestType, approverId, rejectionReason) {
        try {
            const response = await fetch(`${this.baseUrl}/requests/reject/${requestId}?requestType=${requestType}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    approverId,
                    rejectionReason
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '申請の却下に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('申請却下エラー:', error);
            throw error;
        }
    }
}

window.RequestService = RequestService;