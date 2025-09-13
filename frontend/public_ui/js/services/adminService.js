// 管理者サービス（設計書のAPI仕様完全準拠）
class AdminService {
    constructor() {
        this.baseUrl = CONFIG.getApiBaseUrl();
    }

    /**
     * 共通API呼び出し処理
     * @param {string} endpoint - エンドポイント
     * @param {Object} options - オプション
     * @returns {Promise<Object>} API応答
     */
    async apiCall(endpoint, options = {}) {
        try {
            const url = `${this.baseUrl}${endpoint}`;
            const token = localStorage.getItem('kintai_session_token');
            
            const defaultOptions = {
                headers: {
                    'Content-Type': 'application/json',
                    ...(token && { 'Authorization': `Bearer ${token}` })
                }
            };

            const response = await fetch(url, {
                ...defaultOptions,
                ...options,
                headers: {
                    ...defaultOptions.headers,
                    ...options.headers
                }
            });

            const data = await response.json();

            if (response.ok) {
                return {
                    success: true,
                    data: data,
                    message: data.message || 'Success'
                };
            } else {
                return {
                    success: false,
                    message: data.message || `HTTP Error: ${response.status}`,
                    error: data.error || 'Unknown error'
                };
            }
        } catch (error) {
            console.error('API call error:', error);
            return {
                success: false,
                message: 'ネットワークエラーが発生しました',
                error: error.message
            };
        }
    }

    /**
     * 従業員一覧取得（GET /api/admin/employees）
     * @param {Object} params - 検索パラメータ
     * @returns {Promise<Object>} 従業員一覧
     */
    async getEmployees(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return await this.apiCall(`/admin/employees?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * 従業員詳細取得（GET /api/admin/employees/{id}）
     * @param {string} employeeId - 従業員ID
     * @returns {Promise<Object>} 従業員詳細
     */
    async getEmployee(employeeId) {
        return await this.apiCall(`/admin/employees/${employeeId}`, {
            method: 'GET'
        });
    }

    /**
     * 従業員作成（POST /api/admin/employees）
     * @param {Object} employeeData - 従業員データ
     * @returns {Promise<Object>} 作成結果
     */
    async createEmployee(employeeData) {
        return await this.apiCall('/admin/employees', {
            method: 'POST',
            body: JSON.stringify(employeeData)
        });
    }

    /**
     * 従業員更新（PUT /api/admin/employees/{id}）
     * @param {string} employeeId - 従業員ID
     * @param {Object} employeeData - 従業員データ
     * @returns {Promise<Object>} 更新結果
     */
    async updateEmployee(employeeId, employeeData) {
        return await this.apiCall(`/admin/employees/${employeeId}`, {
            method: 'PUT',
            body: JSON.stringify(employeeData)
        });
    }

    /**
     * 従業員削除（DELETE /api/admin/employees/{id}）
     * @param {string} employeeId - 従業員ID
     * @returns {Promise<Object>} 削除結果
     */
    async deleteEmployee(employeeId) {
        return await this.apiCall(`/admin/employees/${employeeId}`, {
            method: 'DELETE'
        });
    }

    /**
     * 承認待ち申請一覧取得（GET /api/admin/requests/pending）
     * @param {Object} params - 検索パラメータ
     * @returns {Promise<Object>} 申請一覧
     */
    async getPendingRequests(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return await this.apiCall(`/admin/requests/pending?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * ダッシュボード統計取得（GET /api/admin/dashboard/stats）
     * @returns {Promise<Object>} 統計データ
     */
    async getDashboardStats() {
        return await this.apiCall('/admin/dashboard/stats', {
            method: 'GET'
        });
    }

    /**
     * 月次レポート生成（GET /api/admin/reports/monthly）
     * @param {string} yearMonth - 年月（YYYY-MM形式）
     * @param {string} employeeId - 従業員ID（オプション）
     * @returns {Promise<Object>} レポート生成結果
     */
    async generateMonthlyReport(yearMonth, employeeId = null) {
        const params = { yearMonth };
        if (employeeId) {
            params.employeeId = employeeId;
        }

        const queryString = new URLSearchParams(params).toString();
        return await this.apiCall(`/admin/reports/monthly?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * 勤怠データ一括更新（POST /api/admin/attendance/bulk-update）
     * @param {Array} attendanceData - 勤怠データ配列
     * @returns {Promise<Object>} 更新結果
     */
    async bulkUpdateAttendance(attendanceData) {
        return await this.apiCall('/admin/attendance/bulk-update', {
            method: 'POST',
            body: JSON.stringify({ attendanceData })
        });
    }
}

// グローバルに公開
window.AdminService = AdminService;
