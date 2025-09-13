// フォーマッター機能テスト
// 設計書の機能仕様に基づく包括的なテストカバレッジ

// テスト対象のモジュールを読み込み
const Formatter = require('../js/utils/formatter');

describe('Formatter', () => {
  describe('minutesToHHMM', () => {
    test('0分', () => {
      expect(Formatter.minutesToHHMM(0)).toBe('00:00');
    });
    
    test('1分', () => {
      expect(Formatter.minutesToHHMM(1)).toBe('00:01');
    });
    
    test('59分', () => {
      expect(Formatter.minutesToHHMM(59)).toBe('00:59');
    });
    
    test('60分 (1時間)', () => {
      expect(Formatter.minutesToHHMM(60)).toBe('01:00');
    });
    
    test('65分 (1時間5分)', () => {
      expect(Formatter.minutesToHHMM(65)).toBe('01:05');
    });
    
    test('480分 (8時間)', () => {
      expect(Formatter.minutesToHHMM(480)).toBe('08:00');
    });
    
    test('1440分 (24時間)', () => {
      expect(Formatter.minutesToHHMM(1440)).toBe('24:00');
    });
    
    test('負数', () => {
      expect(Formatter.minutesToHHMM(-30)).toBe('-00:30');
    });
    
    test('大きな負数', () => {
      expect(Formatter.minutesToHHMM(-120)).toBe('-02:00');
    });
    
    test('null/undefined', () => {
      expect(Formatter.minutesToHHMM(null)).toBe('00:00');
      expect(Formatter.minutesToHHMM(undefined)).toBe('00:00');
    });
    
    test('文字列入力', () => {
      expect(Formatter.minutesToHHMM('60')).toBe('01:00');
    });
    
    test('小数点入力', () => {
      expect(Formatter.minutesToHHMM(60.5)).toBe('01:00');
    });
  });
  
  describe('formatAttendanceStatus', () => {
    test('normal → 出勤', () => {
      expect(Formatter.formatAttendanceStatus('normal')).toBe('出勤');
    });
    
    test('paid_leave → 有給', () => {
      expect(Formatter.formatAttendanceStatus('paid_leave')).toBe('有給');
    });
    
    test('absent → 欠勤', () => {
      expect(Formatter.formatAttendanceStatus('absent')).toBe('欠勤');
    });
    
    test('late → 遅刻', () => {
      expect(Formatter.formatAttendanceStatus('late')).toBe('遅刻');
    });
    
    test('early_leave → 早退', () => {
      expect(Formatter.formatAttendanceStatus('early_leave')).toBe('早退');
    });
    
    test('未定義ステータス', () => {
      expect(Formatter.formatAttendanceStatus('unknown')).toBe('unknown');
    });
    
    test('null/undefined', () => {
      expect(Formatter.formatAttendanceStatus(null)).toBe('');
      expect(Formatter.formatAttendanceStatus(undefined)).toBe('');
    });
    
    test('空文字', () => {
      expect(Formatter.formatAttendanceStatus('')).toBe('');
    });
  });
  
  describe('formatSubmissionStatus', () => {
    test('draft → 下書き', () => {
      expect(Formatter.formatSubmissionStatus('draft')).toBe('下書き');
    });
    
    test('submitted → 申請済', () => {
      expect(Formatter.formatSubmissionStatus('submitted')).toBe('申請済');
    });
    
    test('approved → 承認済', () => {
      expect(Formatter.formatSubmissionStatus('approved')).toBe('承認済');
    });
    
    test('rejected → 却下', () => {
      expect(Formatter.formatSubmissionStatus('rejected')).toBe('却下');
    });
    
    test('未定義ステータス', () => {
      expect(Formatter.formatSubmissionStatus('unknown')).toBe('unknown');
    });
  });
  
  describe('formatErrorMessage', () => {
    test('既知のエラーコード - AUTH_FAILED', () => {
      expect(Formatter.formatErrorMessage('AUTH_FAILED')).toBe('認証に失敗しました');
    });
    
    test('既知のエラーコード - ALREADY_CLOCKED_IN', () => {
      expect(Formatter.formatErrorMessage('ALREADY_CLOCKED_IN')).toBe('既に出勤打刻済みです');
    });
    
    test('既知のエラーコード - ALREADY_CLOCKED_OUT', () => {
      expect(Formatter.formatErrorMessage('ALREADY_CLOCKED_OUT')).toBe('既に退勤打刻済みです');
    });
    
    test('既知のエラーコード - NOT_CLOCKED_IN', () => {
      expect(Formatter.formatErrorMessage('NOT_CLOCKED_IN')).toBe('出勤打刻がされていません');
    });
    
    test('既知のエラーコード - EMPLOYEE_NOT_FOUND', () => {
      expect(Formatter.formatErrorMessage('EMPLOYEE_NOT_FOUND')).toBe('社員が見つかりません');
    });
    
    test('既知のエラーコード - INVALID_REQUEST', () => {
      expect(Formatter.formatErrorMessage('INVALID_REQUEST')).toBe('無効なリクエストです');
    });
    
    test('既知のエラーコード - NETWORK_ERROR', () => {
      expect(Formatter.formatErrorMessage('NETWORK_ERROR')).toBe('ネットワークエラーが発生しました');
    });
    
    test('未知のエラーコード', () => {
      expect(Formatter.formatErrorMessage('UNKNOWN_ERROR')).toBe('エラーが発生しました');
    });
    
    test('デフォルトメッセージ指定', () => {
      const defaultMsg = 'カスタムエラー';
      expect(Formatter.formatErrorMessage('UNKNOWN_ERROR', defaultMsg)).toBe(defaultMsg);
    });
    
    test('null/undefined', () => {
      expect(Formatter.formatErrorMessage(null)).toBe('エラーが発生しました');
      expect(Formatter.formatErrorMessage(undefined)).toBe('エラーが発生しました');
    });
  });
  
  describe('formatCurrency', () => {
    test('正の数値', () => {
      expect(Formatter.formatCurrency(1000)).toBe('¥1,000');
    });
    
    test('負の数値', () => {
      expect(Formatter.formatCurrency(-1000)).toBe('-¥1,000');
    });
    
    test('0', () => {
      expect(Formatter.formatCurrency(0)).toBe('¥0');
    });
    
    test('小数点', () => {
      expect(Formatter.formatCurrency(1000.5)).toBe('¥1,001');
    });
    
    test('大きな数値', () => {
      expect(Formatter.formatCurrency(1000000)).toBe('¥1,000,000');
    });
    
    test('null/undefined', () => {
      expect(Formatter.formatCurrency(null)).toBe('¥0');
      expect(Formatter.formatCurrency(undefined)).toBe('¥0');
    });
  });
  
  describe('formatPercentage', () => {
    test('0%', () => {
      expect(Formatter.formatPercentage(0)).toBe('0%');
    });
    
    test('50%', () => {
      expect(Formatter.formatPercentage(0.5)).toBe('50%');
    });
    
    test('100%', () => {
      expect(Formatter.formatPercentage(1)).toBe('100%');
    });
    
    test('小数点', () => {
      expect(Formatter.formatPercentage(0.1234)).toBe('12.3%');
    });
    
    test('null/undefined', () => {
      expect(Formatter.formatPercentage(null)).toBe('0%');
      expect(Formatter.formatPercentage(undefined)).toBe('0%');
    });
  });
  
  describe('formatFileSize', () => {
    test('バイト', () => {
      expect(Formatter.formatFileSize(500)).toBe('500 B');
    });
    
    test('キロバイト', () => {
      expect(Formatter.formatFileSize(1024)).toBe('1.0 KB');
    });
    
    test('メガバイト', () => {
      expect(Formatter.formatFileSize(1024 * 1024)).toBe('1.0 MB');
    });
    
    test('ギガバイト', () => {
      expect(Formatter.formatFileSize(1024 * 1024 * 1024)).toBe('1.0 GB');
    });
    
    test('null/undefined', () => {
      expect(Formatter.formatFileSize(null)).toBe('0 B');
      expect(Formatter.formatFileSize(undefined)).toBe('0 B');
    });
  });
  
  describe('formatPhoneNumber', () => {
    test('携帯電話番号', () => {
      expect(Formatter.formatPhoneNumber('09012345678')).toBe('090-1234-5678');
    });
    
    test('固定電話番号', () => {
      expect(Formatter.formatPhoneNumber('0312345678')).toBe('03-1234-5678');
    });
    
    test('既にフォーマット済み', () => {
      expect(Formatter.formatPhoneNumber('090-1234-5678')).toBe('090-1234-5678');
    });
    
    test('無効な番号', () => {
      expect(Formatter.formatPhoneNumber('123')).toBe('123');
    });
    
    test('null/undefined', () => {
      expect(Formatter.formatPhoneNumber(null)).toBe('');
      expect(Formatter.formatPhoneNumber(undefined)).toBe('');
    });
  });
  
  describe('truncateText', () => {
    test('短いテキスト', () => {
      expect(Formatter.truncateText('短いテキスト', 10)).toBe('短いテキスト');
    });
    
    test('長いテキスト', () => {
      expect(Formatter.truncateText('これは非常に長いテキストです', 10)).toBe('これは非常に長...');
    });
    
    test('カスタム省略記号', () => {
      expect(Formatter.truncateText('長いテキスト', 5, '...')).toBe('長い...');
    });
    
    test('null/undefined', () => {
      expect(Formatter.truncateText(null, 10)).toBe('');
      expect(Formatter.truncateText(undefined, 10)).toBe('');
    });
  });
});
