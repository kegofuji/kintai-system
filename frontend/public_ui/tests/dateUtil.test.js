// 日付ユーティリティテスト
// 設計書の機能仕様に基づく包括的なテストカバレッジ

// テスト対象のモジュールを読み込み
const DateUtil = require('../js/utils/dateUtil');

describe('DateUtil', () => {
  describe('formatDate', () => {
    test('正常な日付フォーマット', () => {
      const date = new Date('2025-08-01T10:30:00');
      const result = DateUtil.formatDate(date);
      expect(result).toBe('2025-08-01');
    });
    
    test('日付文字列のフォーマット', () => {
      const result = DateUtil.formatDate('2025-08-01T10:30:00');
      expect(result).toBe('2025-08-01');
    });
    
    test('null入力', () => {
      const result = DateUtil.formatDate(null);
      expect(result).toBe('');
    });
    
    test('undefined入力', () => {
      const result = DateUtil.formatDate(undefined);
      expect(result).toBe('');
    });
    
    test('空文字入力', () => {
      const result = DateUtil.formatDate('');
      expect(result).toBe('');
    });
    
    test('無効な日付', () => {
      const result = DateUtil.formatDate('invalid-date');
      expect(result).toBe('');
    });
  });
  
  describe('formatDateJP', () => {
    test('日本語日付フォーマット', () => {
      const result = DateUtil.formatDateJP('2025-08-01');
      expect(result).toMatch(/2025年8月1日\(.*\)/);
    });
    
    test('月曜日の曜日表示', () => {
      const result = DateUtil.formatDateJP('2025-08-04'); // 月曜日
      expect(result).toContain('月');
    });
    
    test('金曜日の曜日表示', () => {
      const result = DateUtil.formatDateJP('2025-08-08'); // 金曜日
      expect(result).toContain('金');
    });
    
    test('土曜日の曜日表示', () => {
      const result = DateUtil.formatDateJP('2025-08-09'); // 土曜日
      expect(result).toContain('土');
    });
    
    test('日曜日の曜日表示', () => {
      const result = DateUtil.formatDateJP('2025-08-10'); // 日曜日
      expect(result).toContain('日');
    });
    
    test('null入力', () => {
      const result = DateUtil.formatDateJP(null);
      expect(result).toBe('');
    });
    
    test('無効な日付', () => {
      const result = DateUtil.formatDateJP('invalid-date');
      expect(result).toBe('');
    });
  });
  
  describe('getWorkingDays', () => {
    test('2025年8月の営業日取得', () => {
      const workingDays = DateUtil.getWorkingDays('2025-08');
      
      // 土日除外チェック
      workingDays.forEach(dateString => {
        const date = new Date(dateString);
        const dayOfWeek = date.getDay();
        expect([0, 6]).not.toContain(dayOfWeek); // 日曜(0)、土曜(6)でない
      });
      
      // 件数チェック（概算）
      expect(workingDays.length).toBeGreaterThan(20);
      expect(workingDays.length).toBeLessThan(25);
    });
    
    test('2025年2月の営業日取得（うるう年）', () => {
      const workingDays = DateUtil.getWorkingDays('2025-02');
      
      // 土日除外チェック
      workingDays.forEach(dateString => {
        const date = new Date(dateString);
        const dayOfWeek = date.getDay();
        expect([0, 6]).not.toContain(dayOfWeek);
      });
      
      // 2月は28日まで
      expect(workingDays.length).toBeGreaterThan(18);
      expect(workingDays.length).toBeLessThan(23);
    });
    
    test('無効な年月形式', () => {
      const result = DateUtil.getWorkingDays('invalid');
      expect(result).toEqual([]);
    });
    
    test('null/undefined入力', () => {
      expect(DateUtil.getWorkingDays(null)).toEqual([]);
      expect(DateUtil.getWorkingDays(undefined)).toEqual([]);
    });
  });
  
  describe('isWorkingDay', () => {
    test('平日判定 - 月曜日', () => {
      const result = DateUtil.isWorkingDay('2025-08-04'); // 月曜日
      expect(result).toBe(true);
    });
    
    test('平日判定 - 金曜日', () => {
      const result = DateUtil.isWorkingDay('2025-08-08'); // 金曜日
      expect(result).toBe(true);
    });
    
    test('休日判定 - 土曜日', () => {
      const result = DateUtil.isWorkingDay('2025-08-09'); // 土曜日
      expect(result).toBe(false);
    });
    
    test('休日判定 - 日曜日', () => {
      const result = DateUtil.isWorkingDay('2025-08-10'); // 日曜日
      expect(result).toBe(false);
    });
    
    test('無効な日付', () => {
      const result = DateUtil.isWorkingDay('invalid-date');
      expect(result).toBe(false);
    });
  });
  
  describe('addDays', () => {
    test('日付加算 - 正の数', () => {
      const result = DateUtil.addDays('2025-08-01', 7);
      expect(result).toBe('2025-08-08');
    });
    
    test('日付加算 - 負の数', () => {
      const result = DateUtil.addDays('2025-08-08', -7);
      expect(result).toBe('2025-08-01');
    });
    
    test('日付加算 - 0', () => {
      const result = DateUtil.addDays('2025-08-01', 0);
      expect(result).toBe('2025-08-01');
    });
    
    test('月をまたぐ加算', () => {
      const result = DateUtil.addDays('2025-08-31', 1);
      expect(result).toBe('2025-09-01');
    });
    
    test('年をまたぐ加算', () => {
      const result = DateUtil.addDays('2025-12-31', 1);
      expect(result).toBe('2026-01-01');
    });
    
    test('無効な日付', () => {
      const result = DateUtil.addDays('invalid-date', 1);
      expect(result).toBe('');
    });
  });
  
  describe('getDateRange', () => {
    test('日付範囲取得 - 1週間', () => {
      const range = DateUtil.getDateRange('2025-08-01', '2025-08-07');
      expect(range).toHaveLength(7);
      expect(range[0]).toBe('2025-08-01');
      expect(range[6]).toBe('2025-08-07');
    });
    
    test('日付範囲取得 - 1日', () => {
      const range = DateUtil.getDateRange('2025-08-01', '2025-08-01');
      expect(range).toHaveLength(1);
      expect(range[0]).toBe('2025-08-01');
    });
    
    test('無効な日付範囲', () => {
      const range = DateUtil.getDateRange('invalid', '2025-08-07');
      expect(range).toEqual([]);
    });
    
    test('開始日が終了日より後', () => {
      const range = DateUtil.getDateRange('2025-08-07', '2025-08-01');
      expect(range).toEqual([]);
    });
  });
  
  describe('getCurrentDateString', () => {
    test('現在日付の文字列取得', () => {
      const result = DateUtil.getCurrentDateString();
      expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    });
    
    test('現在時刻の文字列取得', () => {
      const result = DateUtil.getCurrentTimeString();
      expect(result).toMatch(/^\d{2}:\d{2}$/);
    });
  });
  
  describe('compareDates', () => {
    test('日付比較 - 同じ日付', () => {
      const result = DateUtil.compareDates('2025-08-01', '2025-08-01');
      expect(result).toBe(0);
    });
    
    test('日付比較 - 最初の日付が後', () => {
      const result = DateUtil.compareDates('2025-08-02', '2025-08-01');
      expect(result).toBe(1);
    });
    
    test('日付比較 - 最初の日付が前', () => {
      const result = DateUtil.compareDates('2025-08-01', '2025-08-02');
      expect(result).toBe(-1);
    });
    
    test('無効な日付比較', () => {
      const result = DateUtil.compareDates('invalid', '2025-08-01');
      expect(result).toBe(0);
    });
  });
});
