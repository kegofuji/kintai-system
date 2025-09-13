// 認証サービステスト
// 設計書の機能仕様に基づく包括的なテストカバレッジ

// テスト対象のモジュールを読み込み
const AuthService = require('../js/services/authService');

describe('AuthService', () => {
  let authService;
  
  beforeEach(() => {
    fetch.mockClear();
    localStorage.clear();
    localStorage.getItem.mockReturnValue(null);
    localStorage.setItem.mockClear();
    authService = new AuthService();
  });
  
  describe('login', () => {
    test('ログイン成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          employeeId: 1,
          employeeName: '山田太郎',
          role: 'employee',
          sessionToken: 'mock-token'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await authService.login('E001', 'password');
      
      expect(result).toEqual(mockResponse);
      expect(localStorage.setItem).toHaveBeenCalledWith('kintai_session_token', 'mock-token');
      expect(fetch).toHaveBeenCalledWith('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          employeeCode: 'E001',
          password: 'password'
        })
      });
    });
    
    test('ログイン失敗 - 認証エラー', async () => {
      const mockResponse = {
        success: false,
        errorCode: 'AUTH_FAILED',
        message: '認証に失敗しました'
      };
      
      fetch.mockResolvedValueOnce({
        status: 401,
        json: async () => mockResponse
      });
      
      const result = await authService.login('E001', 'wrong-password');
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('AUTH_FAILED');
      // 認証失敗時はトークンが設定されない（レスポンスにsessionTokenがないため）
      expect(localStorage.setItem).not.toHaveBeenCalled();
    });
    
    test('ログイン失敗 - バリデーションエラー', async () => {
      const mockResponse = {
        success: false,
        errorCode: 'VALIDATION_ERROR',
        message: '社員IDまたはパスワードが正しくありません'
      };
      
      fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => mockResponse
      });
      
      const result = await authService.login('', '');
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('VALIDATION_ERROR');
    });
    
    test('ネットワークエラー', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));
      
      await expect(authService.login('E001', 'password'))
        .rejects.toThrow('NETWORK_ERROR');
    });
    
    test('サーバーエラー', async () => {
      fetch.mockResolvedValueOnce({
        status: 500,
        json: async () => ({
          success: false,
          errorCode: 'INTERNAL_ERROR',
          message: 'サーバーエラーが発生しました'
        })
      });
      
      const result = await authService.login('E001', 'password');
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('INTERNAL_ERROR');
    });
  });
  
  describe('logout', () => {
    test('ログアウト成功', async () => {
      localStorage.getItem.mockReturnValue('mock-token');
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => ({ success: true })
      });
      
      const result = await authService.logout();
      
      expect(result.success).toBe(true);
      expect(localStorage.removeItem).toHaveBeenCalledWith('kintai_session_token');
      expect(fetch).toHaveBeenCalledWith('/api/auth/logout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer mock-token'
        }
      });
    });
    
    test('ログアウト失敗', async () => {
      fetch.mockResolvedValueOnce({
        status: 401,
        json: async () => ({
          success: false,
          errorCode: 'UNAUTHORIZED',
          message: '認証が必要です'
        })
      });
      
      const result = await authService.logout();
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('UNAUTHORIZED');
    });
  });
  
  describe('getCurrentUser', () => {
    test('現在のユーザー情報取得成功', async () => {
      const mockUser = {
        employeeId: 1,
        employeeName: '山田太郎',
        role: 'employee',
        department: '営業部'
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => ({
          success: true,
          data: mockUser
        })
      });
      
      const result = await authService.getCurrentUser();
      
      expect(result.success).toBe(true);
      expect(result.data).toEqual(mockUser);
    });
    
    test('認証エラー', async () => {
      fetch.mockResolvedValueOnce({
        status: 401,
        json: async () => ({
          success: false,
          errorCode: 'UNAUTHORIZED'
        })
      });
      
      const result = await authService.getCurrentUser();
      
      expect(result.success).toBe(false);
      expect(result.errorCode).toBe('UNAUTHORIZED');
    });
  });
  
  describe('isAuthenticated', () => {
    test('認証済み', () => {
      localStorage.getItem.mockReturnValue('mock-token');
      
      const result = authService.isAuthenticated();
      
      expect(result).toBe(true);
    });
    
    test('未認証', () => {
      localStorage.clear();
      
      const result = authService.isAuthenticated();
      
      expect(result).toBe(false);
    });
    
    test('無効なトークン', () => {
      localStorage.setItem('kintai_session_token', '');
      
      const result = authService.isAuthenticated();
      
      expect(result).toBe(false);
    });
  });
  
  describe('getToken', () => {
    test('トークン取得', () => {
      localStorage.getItem.mockReturnValue('mock-token');
      
      const result = authService.getToken();
      
      expect(result).toBe('mock-token');
    });
    
    test('トークンなし', () => {
      localStorage.getItem.mockReturnValue(null);
      
      const result = authService.getToken();
      
      expect(result).toBe(null);
    });
  });
  
  describe('refreshToken', () => {
    test('トークン更新成功', async () => {
      const mockResponse = {
        success: true,
        data: {
          sessionToken: 'new-token'
        }
      };
      
      fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => mockResponse
      });
      
      const result = await authService.refreshToken();
      
      expect(result.success).toBe(true);
      expect(localStorage.setItem).toHaveBeenCalledWith('kintai_session_token', 'new-token');
    });
    
    test('トークン更新失敗', async () => {
      fetch.mockResolvedValueOnce({
        status: 401,
        json: async () => ({
          success: false,
          errorCode: 'TOKEN_EXPIRED'
        })
      });
      
      const result = await authService.refreshToken();
      
      expect(result.success).toBe(false);
      expect(localStorage.removeItem).toHaveBeenCalledWith('kintai_session_token');
    });
  });
});
