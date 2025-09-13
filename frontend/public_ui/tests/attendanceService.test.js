// 勤怠サービステスト
// 設計書の機能仕様に基づく包括的なテストカバレッジ

// テスト対象のモジュールを読み込み
const AttendanceService = require('../js/services/attendanceService');

describe('AttendanceService', () => {
  let attendanceService;
  
  beforeEach(() => {
    fetch.mockClear();
    localStorage.clear();
    localStorage.getItem.mockReturnValue('mock-token');
    attendanceService = new AttendanceService();
  });
  
  describe('clockIn', () => {
    test('出勤打刻成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          clockInTime: '2025-08-01T09:00:00',
          message: '出勤打刻が完了しました'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.clockIn();
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/attendance/clock-in', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer mock-token'
        }
      });
    });
    
    test('出勤打刻失敗 - 既に打刻済み', async () => {
      const mockResponse = {
        success: false,
        errorCode: 'ALREADY_CLOCKED_IN',
        message: '既に出勤打刻済みです'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.clockIn();
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('ALREADY_CLOCKED_IN');
    });
    
    test('出勤打刻失敗 - 認証エラー', async () => {
      fetch.mockResolvedValueOnce({
        status: 401,
        json: async () => ({
          success: false,
          errorCode: 'UNAUTHORIZED'
        })
      });
      
      const result = await attendanceService.clockIn();
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('UNAUTHORIZED');
    });
  });
  
  describe('clockOut', () => {
    test('退勤打刻成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          clockOutTime: '2025-08-01T18:00:00',
          workingMinutes: 480,
          message: '退勤打刻が完了しました'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.clockOut();
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/attendance/clock-out', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer mock-token'
        }
      });
    });
    
    test('退勤打刻失敗 - 出勤打刻なし', async () => {
      const mockResponse = {
        success: false,
        errorCode: 'NOT_CLOCKED_IN',
        message: '出勤打刻がされていません'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.clockOut();
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('NOT_CLOCKED_IN');
    });
    
    test('退勤打刻失敗 - 既に退勤済み', async () => {
      const mockResponse = {
        success: false,
        errorCode: 'ALREADY_CLOCKED_OUT',
        message: '既に退勤打刻済みです'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.clockOut();
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('ALREADY_CLOCKED_OUT');
    });
  });
  
  describe('getAttendanceHistory', () => {
    test('勤怠履歴取得成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          attendanceList: [
            {
              attendanceDate: '2025-08-01',
              clockInTime: '09:00:00',
              clockOutTime: '18:00:00',
              workingMinutes: 480,
              status: 'normal'
            }
          ],
          summary: {
            totalWorkingDays: 1,
            totalWorkingMinutes: 480
          }
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.getAttendanceHistory('2025-08');
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/attendance/history?yearMonth=2025-08', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer mock-token'
        }
      });
    });
    
    test('勤怠履歴取得失敗 - 社員が見つからない', async () => {
      const mockResponse = {
        success: false,
        errorCode: 'EMPLOYEE_NOT_FOUND',
        message: '社員が見つかりません'
      };
      
      fetch.mockResolvedValueOnce({
        status: 404,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.getAttendanceHistory('2025-08');
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('EMPLOYEE_NOT_FOUND');
    });
  });
  
  describe('submitMonthly', () => {
    test('月次申請成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          submissionId: 123,
          message: '月次申請が完了しました'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.submitMonthly('2025-08');
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/attendance/monthly-submit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer mock-token'
        },
        body: JSON.stringify({
          yearMonth: '2025-08'
        })
      });
    });
    
    test('月次申請失敗 - 既に申請済み', async () => {
      const mockResponse = {
        success: false,
        errorCode: 'ALREADY_SUBMITTED',
        message: '既に月次申請済みです'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.submitMonthly('2025-08');
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('ALREADY_SUBMITTED');
    });
  });
  
  describe('getAttendanceStatus', () => {
    test('勤怠状況取得成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          todayStatus: {
            clockedIn: true,
            clockedOut: false,
            clockInTime: '09:00:00',
            workingMinutes: 240
          },
          thisMonthSummary: {
            totalWorkingDays: 15,
            totalWorkingMinutes: 7200,
            averageWorkingMinutes: 480
          }
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.getAttendanceStatus();
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/attendance/status', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer mock-token'
        }
      });
    });
  });
  
  describe('requestAdjustment', () => {
    test('打刻修正申請成功', async () => {
      const adjustmentData = {
        attendanceDate: '2025-08-01',
        clockInTime: '09:00:00',
        clockOutTime: '18:00:00',
        reason: 'システムエラーのため修正申請'
      };
      
      const mockResponse = {
        success: true,
        data: {
          requestId: 456,
          message: '打刻修正申請が完了しました'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.requestAdjustment(adjustmentData);
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/attendance/adjustment', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer mock-token'
        },
        body: JSON.stringify(adjustmentData)
      });
    });
    
    test('打刻修正申請失敗 - バリデーションエラー', async () => {
      const adjustmentData = {
        attendanceDate: '',
        clockInTime: '09:00:00',
        clockOutTime: '18:00:00',
        reason: ''
      };
      
      const mockResponse = {
        success: false,
        errorCode: 'VALIDATION_ERROR',
        message: '必須項目が入力されていません'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await attendanceService.requestAdjustment(adjustmentData);
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('VALIDATION_ERROR');
    });
  });
  
  describe('getAdjustmentRequests', () => {
    test('修正申請一覧取得成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          requests: [
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
      
      const result = await attendanceService.getAdjustmentRequests();
      
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith('/api/attendance/adjustment-requests', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer mock-token'
        }
      });
    });
  });
});
