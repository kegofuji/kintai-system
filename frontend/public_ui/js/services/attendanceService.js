/**
 * 勤怠サービス（出退勤・履歴管理）
 */

class AttendanceService {
    constructor() {
        this.baseUrl = CONFIG.API_BASE_URL;
    }
    
    /**
     * 出勤打刻
     */
    async clockIn(employeeId) {
        try {
            const response = await fetch(`${this.baseUrl}/attendance/clock-in`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ employeeId })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '勤怠履歴の取得に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('勤怠履歴取得エラー:', error);
            throw error;
        }
    }
    
    /**
     * 当日の勤務状況取得
     */
    async getTodayStatus(employeeId) {
        try {
            const response = await fetch(`${this.baseUrl}/attendance/today-status?employeeId=${employeeId}`, {
                method: 'GET',
                credentials: 'include'
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '勤務状況の取得に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('勤務状況取得エラー:', error);
            throw error;
        }
    }
    
    /**
     * 月末申請
     */
    async submitMonthlyAttendance(employeeId, targetMonth) {
        try {
            const response = await fetch(`${this.baseUrl}/attendance/monthly-submit`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({
                    employeeId,
                    targetMonth
                })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '月末申請に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('月末申請エラー:', error);
            throw error;
        }
    }
}

// グローバルに公開
window.AttendanceService = AttendanceService;response.ok) {
                throw new Error(result.message || '出勤打刻に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('出勤打刻エラー:', error);
            throw error;
        }
    }
    
    /**
     * 退勤打刻
     */
    async clockOut(employeeId) {
        try {
            const response = await fetch(`${this.baseUrl}/attendance/clock-out`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ employeeId })
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                throw new Error(result.message || '退勤打刻に失敗しました');
            }
            
            return result;
        } catch (error) {
            console.error('退勤打刻エラー:', error);
            throw error;
        }
    }
    
    /**
     * 勤怠履歴取得
     */
    async getAttendanceHistory(params = {}) {
        try {
            const queryParams = new URLSearchParams();
            
            if (params.employeeId) {
                queryParams.append('employeeId', params.employeeId);
            }
            if (params.yearMonth) {
                queryParams.append('yearMonth', params.yearMonth);
            }
            if (params.dateFrom) {
                queryParams.append('dateFrom', params.dateFrom);
            }
            if (params.dateTo) {
                queryParams.append('dateTo', params.dateTo);
            }
            
            const response = await fetch(`${this.baseUrl}/attendance/history?${queryParams}`, {
                method: 'GET',
                credentials: 'include'
            });
            
            const result = await response.json();
            
            if (!