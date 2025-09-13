// 勤怠サービス（設計書のAPI仕様完全準拠）
class AttendanceService {
    constructor() {
        this.baseUrl = CONFIG.getApiBaseUrl();
        this.pdfUrl = CONFIG.getPdfApiUrl();
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
     * 出勤打刻（POST /api/attendance/clock-in）
     * @returns {Promise<Object>} 打刻結果
     */
    async clockIn() {
        return await this.apiCall('/attendance/clock-in', {
            method: 'POST'
        });
    }

    /**
     * 退勤打刻（POST /api/attendance/clock-out）
     * @returns {Promise<Object>} 打刻結果
     */
    async clockOut() {
        return await this.apiCall('/attendance/clock-out', {
            method: 'POST'
        });
    }

    /**
     * 今日の勤怠情報取得（GET /api/attendance/today）
     * @returns {Promise<Object>} 勤怠情報
     */
    async getTodayAttendance() {
        return await this.apiCall('/attendance/today', {
            method: 'GET'
        });
    }

    /**
     * 勤怠履歴取得（GET /api/attendance/history）
     * @param {string} yearMonth - 年月（YYYY-MM形式）
     * @returns {Promise<Object>} 勤怠履歴
     */
    async getAttendanceHistory(yearMonth) {
        return await this.apiCall(`/attendance/history?yearMonth=${yearMonth}`, {
            method: 'GET'
        });
    }

    /**
     * 勤怠修正申請（POST /api/requests/adjustment）
     * @param {Object} requestData - 申請データ
     * @returns {Promise<Object>} 申請結果
     */
    async submitAdjustmentRequest(requestData) {
        return await this.apiCall('/requests/adjustment', {
            method: 'POST',
            body: JSON.stringify(requestData)
        });
    }

    /**
     * 休暇申請（POST /api/requests/leave）
     * @param {Object} requestData - 申請データ
     * @returns {Promise<Object>} 申請結果
     */
    async submitLeaveRequest(requestData) {
        return await this.apiCall('/requests/leave', {
            method: 'POST',
            body: JSON.stringify(requestData)
        });
    }

    /**
     * PDFレポート生成（POST /reports/pdf）
     * @param {string} yearMonth - 年月（YYYY-MM形式）
     * @returns {Promise<Object>} レポート生成結果
     */
    async generateReport(yearMonth) {
        try {
            const user = JSON.parse(localStorage.getItem(CONFIG.STORAGE_KEYS.USER_INFO));
            const response = await fetch(`${this.pdfUrl}/pdf`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    employee_id: user.employeeId,
                    year_month: yearMonth,
                    report_type: 'monthly'
                })
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
            console.error('Generate report error:', error);
            return {
                success: false,
                message: 'レポート生成に失敗しました',
                error: error.message
            };
        }
    }
}

// グローバルに公開
window.AttendanceService = AttendanceService;
