// バリデーション機能テスト
// 設計書の機能仕様に基づく包括的なテストカバレッジ

// テスト対象のモジュールを読み込み
const Validator = require('../js/utils/validator');

describe('Validator', () => {
  describe('validateEmployeeCode', () => {
    test('正常な社員コード - 3文字', () => {
      const result = Validator.validateEmployeeCode('E01');
      expect(result.valid).toBe(true);
      expect(result.message).toBe('');
    });
    
    test('正常な社員コード - 10文字', () => {
      const result = Validator.validateEmployeeCode('ADMIN12345');
      expect(result.valid).toBe(true);
      expect(result.message).toBe('');
    });
    
    test('正常な社員コード - 英数字混在', () => {
      const result = Validator.validateEmployeeCode('EMP001');
      expect(result.valid).toBe(true);
    });
    
    test('異常ケース - 2文字', () => {
      const result = Validator.validateEmployeeCode('E1');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('社員IDは3-10文字の半角英数字で入力してください');
    });
    
    test('異常ケース - 11文字', () => {
      const result = Validator.validateEmployeeCode('TOOLONGCODE');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('社員IDは3-10文字の半角英数字で入力してください');
    });
    
    test('異常ケース - 日本語文字', () => {
      const result = Validator.validateEmployeeCode('社員001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('社員IDは3-10文字の半角英数字で入力してください');
    });
    
    test('異常ケース - 空文字', () => {
      const result = Validator.validateEmployeeCode('');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('社員IDは3-10文字の半角英数字で入力してください');
    });
    
    test('異常ケース - null/undefined', () => {
      expect(Validator.validateEmployeeCode(null).valid).toBe(false);
      expect(Validator.validateEmployeeCode(undefined).valid).toBe(false);
    });
    
    test('異常ケース - 記号混在', () => {
      const result = Validator.validateEmployeeCode('EMP-001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('社員IDは3-10文字の半角英数字で入力してください');
    });
  });
  
  describe('validatePassword', () => {
    test('正常なパスワード', () => {
      const result = Validator.validatePassword('Admin123!', 'E001');
      expect(result.valid).toBe(true);
      expect(result.message).toBe('');
    });
    
    test('正常なパスワード - 記号混在', () => {
      const result = Validator.validatePassword('Test@123', 'E001');
      expect(result.valid).toBe(true);
    });
    
    test('異常ケース - 7文字', () => {
      const result = Validator.validatePassword('Test1!', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('パスワードは8-20文字の英数字記号を含めて入力してください');
    });
    
    test('異常ケース - 21文字', () => {
      const result = Validator.validatePassword('Test1234567890123456!', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('パスワードは8-20文字の英数字記号を含めて入力してください');
    });
    
    test('異常ケース - 大文字なし', () => {
      const result = Validator.validatePassword('admin123!', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('パスワードは英字（大文字・小文字）、数字、記号を各1文字以上含めて入力してください');
    });
    
    test('異常ケース - 小文字なし', () => {
      const result = Validator.validatePassword('ADMIN123!', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('パスワードは英字（大文字・小文字）、数字、記号を各1文字以上含めて入力してください');
    });
    
    test('異常ケース - 数字なし', () => {
      const result = Validator.validatePassword('AdminTest!', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('パスワードは英字（大文字・小文字）、数字、記号を各1文字以上含めて入力してください');
    });
    
    test('異常ケース - 記号なし', () => {
      const result = Validator.validatePassword('Admin123', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('パスワードは英字（大文字・小文字）、数字、記号を各1文字以上含めて入力してください');
    });
    
    test('異常ケース - 連続同一文字', () => {
      const result = Validator.validatePassword('Aaaa123!', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('連続する同一文字3文字以上は使用できません');
    });
    
    test('異常ケース - 社員IDと同一', () => {
      const result = Validator.validatePassword('E001E001!', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('社員IDと同一のパスワードは使用できません');
    });
    
    test('異常ケース - 空文字', () => {
      const result = Validator.validatePassword('', 'E001');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('パスワードは8-20文字の英数字記号を含めて入力してください');
    });
    
    test('異常ケース - null/undefined', () => {
      expect(Validator.validatePassword(null, 'E001').valid).toBe(false);
      expect(Validator.validatePassword(undefined, 'E001').valid).toBe(false);
    });
  });
  
  describe('validateLeaveDate', () => {
    test('正常ケース - 未来日', () => {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      const dateString = tomorrow.toISOString().split('T')[0];
      
      const result = Validator.validateLeaveDate(dateString);
      expect(result.valid).toBe(true);
      expect(result.message).toBe('');
    });
    
    test('正常ケース - 1週間後', () => {
      const nextWeek = new Date();
      nextWeek.setDate(nextWeek.getDate() + 7);
      const dateString = nextWeek.toISOString().split('T')[0];
      
      const result = Validator.validateLeaveDate(dateString);
      expect(result.valid).toBe(true);
    });
    
    test('異常ケース - 今日', () => {
      const today = new Date().toISOString().split('T')[0];
      const result = Validator.validateLeaveDate(today);
      expect(result.valid).toBe(false);
      expect(result.message).toBe('有給取得日は明日以降を選択してください');
    });
    
    test('異常ケース - 過去日', () => {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      const dateString = yesterday.toISOString().split('T')[0];
      
      const result = Validator.validateLeaveDate(dateString);
      expect(result.valid).toBe(false);
      expect(result.message).toBe('有給取得日は明日以降を選択してください');
    });
    
    test('異常ケース - 無効な日付形式', () => {
      const result = Validator.validateLeaveDate('invalid-date');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('有効な日付を入力してください');
    });
    
    test('異常ケース - 空文字', () => {
      const result = Validator.validateLeaveDate('');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('有効な日付を入力してください');
    });
    
    test('異常ケース - null/undefined', () => {
      expect(Validator.validateLeaveDate(null).valid).toBe(false);
      expect(Validator.validateLeaveDate(undefined).valid).toBe(false);
    });
  });
  
  describe('validateClockTime', () => {
    test('正常な時刻 - 出勤時刻', () => {
      const result = Validator.validateClockTime('09:00', 'clockIn');
      expect(result.valid).toBe(true);
    });
    
    test('正常な時刻 - 退勤時刻', () => {
      const result = Validator.validateClockTime('18:00', 'clockOut');
      expect(result.valid).toBe(true);
    });
    
    test('異常ケース - 無効な時刻形式', () => {
      const result = Validator.validateClockTime('25:00', 'clockIn');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('有効な時刻を入力してください');
    });
    
    test('異常ケース - 文字列', () => {
      const result = Validator.validateClockTime('invalid', 'clockIn');
      expect(result.valid).toBe(false);
    });
    
    test('異常ケース - 空文字', () => {
      const result = Validator.validateClockTime('', 'clockIn');
      expect(result.valid).toBe(false);
    });
  });
  
  describe('validateAdjustmentReason', () => {
    test('正常な理由 - 最小文字数', () => {
      const result = Validator.validateAdjustmentReason('修正');
      expect(result.valid).toBe(true);
    });
    
    test('正常な理由 - 最大文字数', () => {
      const longReason = 'a'.repeat(200);
      const result = Validator.validateAdjustmentReason(longReason);
      expect(result.valid).toBe(true);
    });
    
    test('異常ケース - 空文字', () => {
      const result = Validator.validateAdjustmentReason('');
      expect(result.valid).toBe(false);
      expect(result.message).toBe('修正理由は1-200文字で入力してください');
    });
    
    test('異常ケース - 超過文字数', () => {
      const tooLongReason = 'a'.repeat(201);
      const result = Validator.validateAdjustmentReason(tooLongReason);
      expect(result.valid).toBe(false);
      expect(result.message).toBe('修正理由は1-200文字で入力してください');
    });
    
    test('異常ケース - null/undefined', () => {
      expect(Validator.validateAdjustmentReason(null).valid).toBe(false);
      expect(Validator.validateAdjustmentReason(undefined).valid).toBe(false);
    });
  });
});
