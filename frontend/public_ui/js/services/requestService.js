// 申請サービス（設計書のAPI仕様完全準拠）
class RequestService {
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
     * 休暇申請一覧取得（GET /api/requests/leave）
     * @param {Object} params - 検索パラメータ
     * @returns {Promise<Object>} 申請一覧
     */
    async getLeaveRequests(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return await this.apiCall(`/requests/leave?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * 勤怠修正申請一覧取得（GET /api/requests/adjustment）
     * @param {Object} params - 検索パラメータ
     * @returns {Promise<Object>} 申請一覧
     */
    async getAdjustmentRequests(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return await this.apiCall(`/requests/adjustment?${queryString}`, {
            method: 'GET'
        });
    }

    /**
     * 申請詳細取得（GET /api/requests/{type}/{id}）
     * @param {string} requestId - 申請ID
     * @param {string} requestType - 申請タイプ
     * @returns {Promise<Object>} 申請詳細
     */
    async getRequestDetail(requestId, requestType) {
        return await this.apiCall(`/requests/${requestType}/${requestId}`, {
            method: 'GET'
        });
    }

    /**
     * 申請更新（PUT /api/requests/{type}/{id}）
     * @param {string} requestId - 申請ID
     * @param {string} requestType - 申請タイプ
     * @param {Object} updateData - 更新データ
     * @returns {Promise<Object>} 更新結果
     */
    async updateRequest(requestId, requestType, updateData) {
        return await this.apiCall(`/requests/${requestType}/${requestId}`, {
            method: 'PUT',
            body: JSON.stringify(updateData)
        });
    }

    /**
     * 申請削除（DELETE /api/requests/{type}/{id}）
     * @param {string} requestId - 申請ID
     * @param {string} requestType - 申請タイプ
     * @returns {Promise<Object>} 削除結果
     */
    async deleteRequest(requestId, requestType) {
        return await this.apiCall(`/requests/${requestType}/${requestId}`, {
            method: 'DELETE'
        });
    }

    /**
     * 申請承認（POST /api/requests/{type}/{id}/approve）
     * @param {string} requestId - 申請ID
     * @param {string} requestType - 申請タイプ
     * @param {string} comment - コメント
     * @returns {Promise<Object>} 承認結果
     */
    async approveRequest(requestId, requestType, comment = '') {
        return await this.apiCall(`/requests/${requestType}/${requestId}/approve`, {
            method: 'POST',
            body: JSON.stringify({ comment })
        });
    }

    /**
     * 申請却下（POST /api/requests/{type}/{id}/reject）
     * @param {string} requestId - 申請ID
     * @param {string} requestType - 申請タイプ
     * @param {string} comment - 却下理由
     * @returns {Promise<Object>} 却下結果
     */
    async rejectRequest(requestId, requestType, comment = '') {
        return await this.apiCall(`/requests/${requestType}/${requestId}/reject`, {
            method: 'POST',
            body: JSON.stringify({ comment })
        });
    }
}

// グローバルに公開
window.RequestService = RequestService;
