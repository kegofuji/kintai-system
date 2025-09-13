// フォーマッターユーティリティ（設計書の時間表示要件準拠）
class Formatter {
    /**
     * 分をHH:MM形式に変換（設計書：分→時分変換）
     */
    static minutesToHHMM(minutes) {
        if (minutes === 0 || minutes === null || minutes === undefined) {
            return '00:00';
        }
        
        const hours = Math.floor(Math.abs(minutes) / 60);
        const mins = Math.abs(minutes) % 60;
        const sign = minutes < 0 ? '-' : '';
        
        return `${sign}${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
    }
    
    /**
     * 数値をカンマ区切りに変換
     */
    static numberWithCommas(num) {
        if (num === null || num === undefined) return '0';
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }
    
    /**
     * ステータス日本語変換
     */
    static formatAttendanceStatus(status) {
        const statusMap = {
            'normal': '出勤',
            'paid_leave': '有給',
            'absent': '欠勤'
        };
        return statusMap[status] || status;
    }
    
    /**
     * 申請状況日本語変換
     */
    static formatSubmissionStatus(status) {
        const statusMap = {
            '未提出': '未提出',
            '申請済': '申請済',
            '承認': '確定済',
            '却下': '却下'
        };
        return statusMap[status] || status;
    }
    
    /**
     * 申請種別日本語変換
     */
    static formatRequestType(type) {
        const typeMap = {
            'leave': '有給申請',
            'adjustment': '打刻修正',
            'monthly': '月末申請'
        };
        return typeMap[type] || type;
    }
    
    /**
     * 申請状態日本語変換
     */
    static formatRequestStatus(status) {
        const statusMap = {
            '未処理': '未処理',
            '承認': '承認済',
            '却下': '却下済'
        };
        return statusMap[status] || status;
    }
    
    /**
     * ファイルサイズフォーマット
     */
    static formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
    
    /**
     * エラーメッセージフォーマット（設計書エラーコード対応）
     */
    static formatErrorMessage(errorCode, defaultMessage) {
        const errorMessages = {
            'AUTH_FAILED': '認証に失敗しました',
            'SESSION_TIMEOUT': 'セッションがタイムアウトしました',
            'ACCESS_DENIED': 'アクセス権限がありません',
            'ALREADY_CLOCKED_IN': '既に出勤打刻済みです',
            'NOT_CLOCKED_IN': '出勤打刻が必要です',
            'INCOMPLETE_ATTENDANCE': '打刻漏れがあります',
            'INSUFFICIENT_LEAVE_DAYS': '有給残日数が不足しています',
            'DUPLICATE_REQUEST': '既に申請済みです',
            'FIXED_ATTENDANCE': '確定済みのため変更できません',
            'EMPLOYEE_NOT_FOUND': '社員が見つかりません',
            'REQUEST_NOT_FOUND': '申請が見つかりません',
            'VALIDATION_ERROR': '入力内容に誤りがあります',
            'SYSTEM_ERROR': 'システムエラーが発生しました',
            'NETWORK_ERROR': 'ネットワークエラーが発生しました'
        };
        
        return errorMessages[errorCode] || defaultMessage || 'エラーが発生しました';
    }
    
    /**
     * 日時フォーマット（YYYY/MM/DD HH:MM）
     */
    static datetime(date) {
        if (!date) return '';
        
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        
        return `${year}/${month}/${day} ${hours}:${minutes}`;
    }
    
    /**
     * 日付フォーマット（YYYY/MM/DD）
     */
    static date(date) {
        if (!date) return '';
        
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        
        return `${year}/${month}/${day}`;
    }
    
    /**
     * 時刻フォーマット（HH:MM）
     */
    static time(date) {
        if (!date) return '';
        
        const d = new Date(date);
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        
        return `${hours}:${minutes}`;
    }
    
    /**
     * 通貨フォーマット（円）
     */
    static currency(amount) {
        if (amount === null || amount === undefined) return '¥0';
        return '¥' + this.numberWithCommas(amount);
    }
    
    /**
     * パーセンテージフォーマット
     */
    static percentage(value, decimals = 1) {
        if (value === null || value === undefined) return '0%';
        return (value * 100).toFixed(decimals) + '%';
    }
    
    /**
     * 勤務時間フォーマット（時間:分）
     */
    static workHours(minutes) {
        if (minutes === null || minutes === undefined) return '0:00';
        
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        
        return `${hours}:${String(mins).padStart(2, '0')}`;
    }
    
    /**
     * 残業時間フォーマット（時間:分）
     */
    static overtimeHours(minutes) {
        if (minutes === null || minutes === undefined || minutes === 0) return '0:00';
        
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        
        return `${hours}:${String(mins).padStart(2, '0')}`;
    }
    
    /**
     * 遅刻時間フォーマット（分）
     */
    static lateMinutes(minutes) {
        if (minutes === null || minutes === undefined || minutes === 0) return '0分';
        return `${minutes}分`;
    }
    
    /**
     * 早退時間フォーマット（分）
     */
    static earlyLeaveMinutes(minutes) {
        if (minutes === null || minutes === undefined || minutes === 0) return '0分';
        return `${minutes}分`;
    }
    
    /**
     * 有給日数フォーマット
     */
    static leaveDays(days) {
        if (days === null || days === undefined) return '0日';
        return `${days}日`;
    }
    
    /**
     * 社員コードフォーマット（大文字変換）
     */
    static employeeCode(code) {
        if (!code) return '';
        return code.toUpperCase();
    }
    
    /**
     * 氏名フォーマット（前後の空白除去）
     */
    static employeeName(name) {
        if (!name) return '';
        return name.trim();
    }
    
    /**
     * メールアドレスフォーマット（小文字変換）
     */
    static email(email) {
        if (!email) return '';
        return email.toLowerCase().trim();
    }
    
    /**
     * 電話番号フォーマット（ハイフン区切り）
     */
    static phoneNumber(phone) {
        if (!phone) return '';
        
        // 数字のみ抽出
        const numbers = phone.replace(/\D/g, '');
        
        if (numbers.length === 11) {
            // 携帯電話: 090-1234-5678
            return numbers.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
        } else if (numbers.length === 10) {
            // 固定電話: 03-1234-5678
            return numbers.replace(/(\d{2,4})(\d{4})(\d{4})/, '$1-$2-$3');
        }
        
        return phone;
    }
    
    /**
     * 郵便番号フォーマット（ハイフン区切り）
     */
    static postalCode(code) {
        if (!code) return '';
        
        // 数字のみ抽出
        const numbers = code.replace(/\D/g, '');
        
        if (numbers.length === 7) {
            return numbers.replace(/(\d{3})(\d{4})/, '$1-$2');
        }
        
        return code;
    }
    
    /**
     * 住所フォーマット（都道府県 + 市区町村 + その他）
     */
    static address(prefecture, city, address) {
        const parts = [prefecture, city, address].filter(part => part && part.trim());
        return parts.join('');
    }
}

// グローバルに公開
window.Formatter = Formatter;