// 申請サービステスト
// 設計書の機能仕様に基づく包括的なテストカバレッジ

// テスト対象のモジュールを読み込み
const RequestService = require('../js/services/requestService');

describe('RequestService', () => {
  let requestService;
  
  beforeEach(() => {
    fetch.mockClear();
    localStorage.clear();
    localStorage.getItem.mockReturnValue('mock-token');
    requestService = new RequestService();
  });
  
  describe('submitLeaveRequest', () => {
    test('有給申請成功', async () => {
      const leaveData = {
        leaveDate: '2025-08-15',
        leaveType: 'paid_leave',
        reason: '家族の用事のため'
      };
      
      const mockResponse = {
        success: true,
        data: {
          requestId: 789,
          message: '有給申請が完了しました'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await requestService.submitLeaveRequest(leaveData);
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/requests/leave', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer mock-token'
        },
        body: JSON.stringify(leaveData)
      });
    });
    
    test('有給申請失敗 - 残日数不足', async () => {
      const leaveData = {
        leaveDate: '2025-08-15',
        leaveType: 'paid_leave',
        reason: '家族の用事のため'
      };
      
      const mockResponse = {
        success: false,
        errorCode: 'INSUFFICIENT_LEAVE_DAYS',
        message: '有給残日数が不足しています'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await requestService.submitLeaveRequest(leaveData);
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('INSUFFICIENT_LEAVE_DAYS');
    });
    
    test('有給申請失敗 - 過去日指定', async () => {
      const leaveData = {
        leaveDate: '2025-07-01',
        leaveType: 'paid_leave',
        reason: '家族の用事のため'
      };
      
      const mockResponse = {
        success: false,
        errorCode: 'INVALID_DATE',
        message: '有給取得日は明日以降を選択してください'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await requestService.submitLeaveRequest(leaveData);
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('INVALID_DATE');
    });
  });
  
  describe('getLeaveRequests', () => {
    test('有給申請一覧取得成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          requests: [
            {
              requestId: 789,
              leaveDate: '2025-08-15',
              leaveType: 'paid_leave',
              status: 'pending',
              reason: '家族の用事のため',
              createdAt: '2025-08-01T10:00:00'
            }
          ],
          summary: {
            totalRequests: 1,
            pendingRequests: 1,
            approvedRequests: 0,
            rejectedRequests: 0
          }
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await requestService.getLeaveRequests();
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/requests/leave', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer mock-token'
        }
      });
    });
    
    test('有給申請一覧取得失敗 - 認証エラー', async () => {
      fetch.mockResolvedValueOnce({
        status: 401,
        json: async () => ({
          success: false,
          errorCode: 'UNAUTHORIZED'
        })
      });
      
      const result = await requestService.getLeaveRequests();
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('UNAUTHORIZED');
    });
  });
  
  describe('cancelLeaveRequest', () => {
    test('有給申請取消成功', async () => {
      const requestId = 789;
      
      const mockResponse = {
        success: true,
        data: {
          message: '有給申請が取消されました'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await requestService.cancelLeaveRequest(requestId);
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith(`/api/requests/leave/${requestId}/cancel`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer mock-token'
        }
      });
    });
    
    test('有給申請取消失敗 - 申請が見つからない', async () => {
      const requestId = 999;
      
      const mockResponse = {
        success: false,
        errorCode: 'REQUEST_NOT_FOUND',
        message: '申請が見つかりません'
      };
      
      fetch.mockResolvedValueOnce({
        status: 404,
        json: async () => mockResponse
      });
      
      const result = await requestService.cancelLeaveRequest(requestId);
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('REQUEST_NOT_FOUND');
    });
    
    test('有給申請取消失敗 - 既に処理済み', async () => {
      const requestId = 789;
      
      const mockResponse = {
        success: false,
        errorCode: 'ALREADY_PROCESSED',
        message: '既に処理済みの申請は取消できません'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await requestService.cancelLeaveRequest(requestId);
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('ALREADY_PROCESSED');
    });
  });
  
  describe('getLeaveBalance', () => {
    test('有給残日数取得成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          totalDays: 20,
          usedDays: 5,
          remainingDays: 15,
          fiscalYear: '2025'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await requestService.getLeaveBalance();
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/requests/leave-balance', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer mock-token'
        }
      });
    });
  });
  
  describe('getRequestHistory', () => {
    test('申請履歴取得成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          leaveRequests: [
            {
              requestId: 789,
              leaveDate: '2025-08-15',
              leaveType: 'paid_leave',
              status: 'approved',
              reason: '家族の用事のため',
              createdAt: '2025-08-01T10:00:00',
              processedAt: '2025-08-02T14:30:00'
            }
          ],
          adjustmentRequests: [
            {
              requestId: 456,
              attendanceDate: '2025-08-01',
              status: 'pending',
              reason: 'システムエラーのため修正申請',
              createdAt: '2025-08-01T10:00:00'
            }
          ]
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await requestService.getRequestHistory();
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/requests/history', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer mock-token'
        }
      });
    });
  });
  
  describe('getRequestStatus', () => {
    test('申請状況取得成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          pendingCount: 2,
          approvedCount: 5,
          rejectedCount: 1,
          totalCount: 8
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await requestService.getRequestStatus();
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/requests/status', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer mock-token'
        }
      });
    });
  });
});
