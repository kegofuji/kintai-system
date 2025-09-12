/**
 * 日付・時刻ユーティリティクラス（日本時間対応）
 */

class DateUtil {
    /**
     * 現在の日本時間取得
     */
    static nowInJapan() {
        return new Date(new Date().toLocaleString("en-US", {timeZone: "Asia/Tokyo"}));
    }
    
    /**
     * 今日の日付（日本時間）
     */
    static todayInJapan() {
        const now = this.nowInJapan();
        return new Date(now.getFullYear(), now.getMonth(), now.getDate());
    }
    
    /**
     * 日付フォーマット（YYYY/MM/DD）
     */
    static formatDate(date) {
        if (!date) return '';
        
        const d = new Date(date);
        if (isNaN(d.getTime())) return '';
        
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        
        return `${year}/${month}/${day}`;
    }
    
    /**
     * 時刻フォーマット（HH:mm）
     */
    static formatTime(datetime) {
        if (!datetime) return '';
        
        const d = new Date(datetime);
        if (isNaN(d.getTime())) return '';
        
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        
        return `${hours}:${minutes}`;
    }
    
    /**
     * 時刻のみ取得（HH:mm）
     */
    static formatTimeOnly(timeString) {
        if (!timeString) return '--:--';
        
        // "HH:mm:ss" または "2025-08-01T09:05:00" 形式に対応
        if (timeString.includes('T')) {
            const d = new Date(timeString);
            return this.formatTime(d);
        }
        
        // "HH:mm:ss" 形式
        const timeParts = timeString.split(':');
        if (timeParts.length >= 2) {
            return `${timeParts[0]}:${timeParts[1]}`;
        }
        
        return timeString;
    }
    
    /**
     * 日付時刻フォーマット（YYYY/MM/DD HH:mm）
     */
    static formatDateTime(datetime) {
        if (!datetime) return '';
        
        const d = new Date(datetime);
        if (isNaN(d.getTime())) return '';
        
        return `${this.formatDate(d)} ${this.formatTime(d)}`;
    }
    
    /**
     * 入力用日付フォーマット（YYYY-MM-DD）
     */
    static formatDateForInput(date) {
        if (!date) return '';
        
        const d = new Date(date);
        if (isNaN(d.getTime())) return '';
        
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        
        return `${year}-${month}-${day}`;
    }
    
    /**
     * 月初日取得
     */
    static getFirstDayOfMonth(year, month) {
        return new Date(year, month - 1, 1);
    }
    
    /**
     * 月末日取得
     */
    static getLastDayOfMonth(year, month) {
        return new Date(year, month, 0);
    }
    
    /**
     * 年月文字列から月初・月末日取得
     */
    static getMonthRange(yearMonthString) {
        if (!yearMonthString || !yearMonthString.match(/^\d{4}-\d{2}$/)) {
            return null;
        }
        
        const [year, month] = yearMonthString.split('-').map(Number);
        
        return {
            start: this.getFirstDayOfMonth(year, month),
            end: this.getLastDayOfMonth(year, month),
            startFormatted: this.formatDateForInput(this.getFirstDayOfMonth(year, month)),
            endFormatted: this.formatDateForInput(this.getLastDayOfMonth(year, month))
        };
    }
    
    /**
     * 営業日判定（土日のみ除外、祝日は考慮しない）
     */
    static isWorkingDay(date) {
        const d = new Date(date);
        const dayOfWeek = d.getDay(); // 0: 日曜, 1: 月曜, ..., 6: 土曜
        
        return dayOfWeek !== 0 && dayOfWeek !== 6; // 土日以外
    }
    
    /**
     * 指定月の営業日一覧取得
     */
    static getWorkingDaysInMonth(year, month) {
        const workingDays = [];
        const firstDay = this.getFirstDayOfMonth(year, month);
        const lastDay = this.getLastDayOfMonth(year, month);
        
        let currentDate = new Date(firstDay);
        
        while (currentDate <= lastDay) {
            if (this.isWorkingDay(currentDate)) {
                workingDays.push(new Date(currentDate));
            }
            currentDate.setDate(currentDate.getDate() + 1);
        }
        
        return workingDays;
    }
    
    /**
     * 日付文字列をDateオブジェクトに変換
     */
    static parseDate(dateString) {
        if (!dateString) return null;
        
        // YYYY-MM-DD 形式
        if (dateString.match(/^\d{4}-\d{2}-\d{2}$/)) {
            return new Date(dateString + 'T00:00:00');
        }
        
        // YYYY/MM/DD 形式
        if (dateString.match(/^\d{4}\/\d{2}\/\d{2}$/)) {
            const parts = dateString.split('/');
            return new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
        }
        
        // その他の形式
        const date = new Date(dateString);
        return isNaN(date.getTime()) ? null : date;
    }
    
    /**
     * 時刻文字列をDateオブジェクトに変換（今日の日付で）
     */
    static parseTime(timeString) {
        if (!timeString) return null;
        
        const timeParts = timeString.split(':');
        if (timeParts.length < 2) return null;
        
        const today = this.todayInJapan();
        today.setHours(parseInt(timeParts[0]), parseInt(timeParts[1]), 0, 0);
        
        return today;
    }
    
    /**
     * 日付の妥当性チェック
     */
    static isValidDate(dateString) {
        const date = this.parseDate(dateString);
        return date !== null && !isNaN(date.getTime());
    }
    
    /**
     * 未来日チェック
     */
    static isFutureDate(dateString) {
        const date = this.parseDate(dateString);
        if (!date) return false;
        
        const today = this.todayInJapan();
        return date > today;
    }
    
    /**
     * 過去日チェック
     */
    static isPastDate(dateString) {
        const date = this.parseDate(dateString);
        if (!date) return false;
        
        const today = this.todayInJapan();
        return date < today;
    }
    
    /**
     * 当日チェック
     */
    static isToday(dateString) {
        const date = this.parseDate(dateString);
        if (!date) return false;
        
        const today = this.todayInJapan();
        return date.toDateString() === today.toDateString();
    }
    
    /**
     * 日付差分計算（日数）
     */
    static getDaysDifference(date1, date2) {
        const d1 = new Date(date1);
        const d2 = new Date(date2);
        
        if (isNaN(d1.getTime()) || isNaN(d2.getTime())) return 0;
        
        const timeDiff = d2.getTime() - d1.getTime();
        return Math.ceil(timeDiff / (1000 * 3600 * 24));
    }
    
    /**
     * 月間カレンダー生成
     */
    static generateMonthCalendar(year, month) {
        const firstDay = this.getFirstDayOfMonth(year, month);
        const lastDay = this.getLastDayOfMonth(year, month);
        const startDate = new Date(firstDay);
        const endDate = new Date(lastDay);
        
        // 月初の曜日まで前月の日付で埋める
        startDate.setDate(startDate.getDate() - firstDay.getDay());
        
        // 月末の曜日まで翌月の日付で埋める
        endDate.setDate(endDate.getDate() + (6 - lastDay.getDay()));
        
        const calendar = [];
        const currentDate = new Date(startDate);
        
        while (currentDate <= endDate) {
            calendar.push({
                date: new Date(currentDate),
                dateString: this.formatDateForInput(currentDate),
                displayString: this.formatDate(currentDate),
                isCurrentMonth: currentDate.getMonth() === month - 1,
                isWorkingDay: this.isWorkingDay(currentDate),
                isToday: this.isToday(this.formatDateForInput(currentDate))
            });
            
            currentDate.setDate(currentDate.getDate() + 1);
        }
        
        return calendar;
    }
    
    /**
     * 相対日付文字列生成
     */
    static getRelativeDateString(dateString) {
        const date = this.parseDate(dateString);
        if (!date) return '';
        
        const today = this.todayInJapan();
        const daysDiff = this.getDaysDifference(today, date);
        
        if (daysDiff === 0) {
            return '今日';
        } else if (daysDiff === 1) {
            return '明日';
        } else if (daysDiff === -1) {
            return '昨日';
        } else if (daysDiff > 0 && daysDiff <= 7) {
            return `${daysDiff}日後`;
        } else if (daysDiff < 0 && daysDiff >= -7) {
            return `${Math.abs(daysDiff)}日前`;
        } else {
            return this.formatDate(date);
        }
    }
    
    /**
     * 現在時刻の時間範囲チェック
     */
    static isCurrentTimeInRange(startTime, endTime) {
        const now = this.nowInJapan();
        const currentTime = now.getHours() * 60 + now.getMinutes();
        
        const start = this.parseTimeToMinutes(startTime);
        const end = this.parseTimeToMinutes(endTime);
        
        if (start === null || end === null) return false;
        
        // 日をまたぐ場合（例：22:00-05:00）
        if (start > end) {
            return currentTime >= start || currentTime <= end;
        } else {
            return currentTime >= start && currentTime <= end;
        }
    }
    
    /**
     * 時刻文字列を分に変換
     */
    static parseTimeToMinutes(timeString) {
        if (!timeString) return null;
        
        const parts = timeString.split(':');
        if (parts.length < 2) return null;
        
        const hours = parseInt(parts[0]);
        const minutes = parseInt(parts[1]);
        
        if (isNaN(hours) || isNaN(minutes)) return null;
        
        return hours * 60 + minutes;
    }
    
    /**
     * 分を時刻文字列に変換
     */
    static minutesToTimeString(minutes) {
        if (typeof minutes !== 'number' || minutes < 0) return '00:00';
        
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        
        return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
    }
}

// グローバルに公開
window.DateUtil = DateUtil;