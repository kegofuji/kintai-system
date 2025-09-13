// 日付処理ユーティリティ（設計書の日付処理要件準拠）
class DateUtil {
    /**
     * 現在日時取得（Asia/Tokyoタイムゾーン想定）
     */
    static getCurrentDateTime() {
        return new Date();
    }
    
    /**
     * 現在日付取得（YYYY-MM-DD形式）
     */
    static getCurrentDate() {
        return DateUtil.formatDate(new Date());
    }
    
    /**
     * 日付フォーマット（YYYY-MM-DD）
     */
    static formatDate(date) {
        if (!date) return '';
        
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        
        return `${year}-${month}-${day}`;
    }
    
    /**
     * 時刻フォーマット（HH:MM）
     */
    static formatTime(date) {
        if (!date) return '';
        
        const d = new Date(date);
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        
        return `${hours}:${minutes}`;
    }
    
    /**
     * 年月フォーマット（YYYY-MM）
     */
    static formatYearMonth(date) {
        if (!date) return '';
        
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        
        return `${year}-${month}`;
    }
    
    /**
     * 営業日取得（土日除外、祝日は除外しない簡易版）
     */
    static getWorkingDays(yearMonth) {
        const [year, month] = yearMonth.split('-').map(Number);
        const startDate = new Date(year, month - 1, 1);
        const endDate = new Date(year, month, 0);
        
        const workingDays = [];
        
        for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
            const dayOfWeek = d.getDay();
            // 0=日曜日, 6=土曜日を除外
            if (dayOfWeek !== 0 && dayOfWeek !== 6) {
                workingDays.push(DateUtil.formatDate(new Date(d)));
            }
        }
        
        return workingDays;
    }
    
    /**
     * 月の期間取得
     */
    static getMonthRange(yearMonth) {
        const [year, month] = yearMonth.split('-').map(Number);
        const startDate = new Date(year, month - 1, 1);
        const endDate = new Date(year, month, 0);
        
        return {
            from: DateUtil.formatDate(startDate),
            to: DateUtil.formatDate(endDate)
        };
    }
    
    /**
     * 日付文字列を日本語形式に変換
     */
    static formatDateJP(dateString) {
        if (!dateString) return '';
        
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const dayOfWeek = ['日', '月', '火', '水', '木', '金', '土'][date.getDay()];
        
        return `${year}年${month}月${day}日(${dayOfWeek})`;
    }
    
    /**
     * 相対日付取得
     */
    static getRelativeDate(days) {
        const date = new Date();
        date.setDate(date.getDate() + days);
        return DateUtil.formatDate(date);
    }
    
    /**
     * 今月取得（YYYY-MM形式）
     */
    static getCurrentYearMonth() {
        return DateUtil.formatYearMonth(new Date());
    }
    
    /**
     * 前月取得（YYYY-MM形式）
     */
    static getPreviousYearMonth() {
        const date = new Date();
        date.setMonth(date.getMonth() - 1);
        return DateUtil.formatYearMonth(date);
    }
    
    /**
     * 翌月取得（YYYY-MM形式）
     */
    static getNextYearMonth() {
        const date = new Date();
        date.setMonth(date.getMonth() + 1);
        return DateUtil.formatYearMonth(date);
    }
    
    /**
     * 日付の差分日数取得
     */
    static getDaysDifference(date1, date2) {
        const d1 = new Date(date1);
        const d2 = new Date(date2);
        const diffTime = Math.abs(d2 - d1);
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    }
    
    /**
     * 月末日取得
     */
    static getLastDayOfMonth(yearMonth) {
        const [year, month] = yearMonth.split('-').map(Number);
        const lastDay = new Date(year, month, 0);
        return lastDay.getDate();
    }
    
    /**
     * 週の開始日（月曜日）取得
     */
    static getWeekStart(date) {
        const d = new Date(date);
        const day = d.getDay();
        const diff = d.getDate() - day + (day === 0 ? -6 : 1); // 月曜日を週の開始とする
        d.setDate(diff);
        return DateUtil.formatDate(d);
    }
    
    /**
     * 週の終了日（日曜日）取得
     */
    static getWeekEnd(date) {
        const d = new Date(date);
        const day = d.getDay();
        const diff = d.getDate() - day + 7; // 日曜日を週の終了とする
        d.setDate(diff);
        return DateUtil.formatDate(d);
    }
    
    /**
     * 日付が有効かチェック
     */
    static isValidDate(dateString) {
        const date = new Date(dateString);
        return !isNaN(date.getTime());
    }
    
    /**
     * 日付文字列をDateオブジェクトに変換
     */
    static parseDate(dateString) {
        if (!dateString) return null;
        const date = new Date(dateString);
        return isNaN(date.getTime()) ? null : date;
    }
    
    /**
     * 日付比較（同じ日かどうか）
     */
    static isSameDate(date1, date2) {
        const d1 = new Date(date1);
        const d2 = new Date(date2);
        return d1.getFullYear() === d2.getFullYear() &&
               d1.getMonth() === d2.getMonth() &&
               d1.getDate() === d2.getDate();
    }
    
    /**
     * 日付が範囲内かチェック
     */
    static isDateInRange(date, startDate, endDate) {
        const d = new Date(date);
        const start = new Date(startDate);
        const end = new Date(endDate);
        return d >= start && d <= end;
    }
}

// グローバルに公開
window.DateUtil = DateUtil;