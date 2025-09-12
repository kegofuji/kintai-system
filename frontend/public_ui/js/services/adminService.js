/**
 * 管理者サービス（社員管理・レポート出力）
 */

class AdminService {
    constructor() {
        this.baseUrl = CONFIG.API_BASE_URL;
    }
    
    /**
     * 社員一覧取得
     */
    async getEmployees(params = {}) {
        try {
            const queryParams = new URLSearchParams();
            
            if (params.status) {
                queryParams.append('status', params.status);
            }
            if (params.keyword) {
                queryParams.append('keyword', params.keyword);
            }
            
            const response = await fetch(`${this.baseUrl}/admin/employees?${queryParams}`, {
                method: 'GET',
                credentials: 'include'
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '社員一覧の取得に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('社員一覧取得エラー:', error);
            throw error;
        }
    }
    
    /**
     * 社員追加
     */
    async addEmployee(employeeData) {
        try {
            const response = await fetch(`${this.baseUrl}/admin/employees`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(employeeData)
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '社員の追加に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('社員追加エラー:', error);
            throw error;
        }
    }
    
    /**
     * 社員情報更新
     */
    async updateEmployee(employeeId, updateData) {
        try {
            const response = await fetch(`${this.baseUrl}/admin/employees/${employeeId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(updateData)
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '社員情報の更新に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('社員更新エラー:', error);
            throw error;
        }
    }
    
    /**
     * 退職処理
     */
    async retireEmployee(employeeId, retiredAt) {
        try {
            const response = await fetch(`${this.baseUrl}/admin/employees/${employeeId}/retire`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ retiredAt })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '退職処理に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('退職処理エラー:', error);
            throw error;
        }
    }
    
    /**
     * 有給日数調整
     */
    async adjustPaidLeave(employeeId, adjustmentDays, reason) {
        try {
            const response = await fetch(`${this.baseUrl}/admin/paid-leave/adjust`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    employeeId,
                    adjustmentDays,
                    reason
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '有給日数調整に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('有給調整エラー:', error);
            throw error;
        }
    }
    
    /**
     * PDFレポート生成
     */
    async generateReport(employeeId, yearMonth) {
        try {
            const response = await fetch(`${this.baseUrl}/reports/generate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    employeeId,
                    yearMonth,
                    reportType: 'monthly'
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || 'レポート生成に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('レポート生成エラー:', error);
            throw error;
        }
    }
}

window.AdminService = AdminService;