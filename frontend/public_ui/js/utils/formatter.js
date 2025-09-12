/**
 * データフォーマッター（表示形式）
 */

class Formatter {
    /**
     * 分を時間:分の文字列に変換（hh:mm表示）
     */
    static formatMinutesToHHMM(minutes) {
        if (typeof minutes !== 'number' || minutes === 0 || minutes === null) {
            return '00:00';
        }
        
        const hours = Math.floor(Math.abs(minutes) / 60);
        const mins = Math.abs(minutes) % 60;
        const sign = minutes < 0 ? '-' : '';
        
        return `${sign}${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
    }
    
    /**
     * ステータス表示名取得（日本語表示）
     */
    static formatStatus(status, type) {
        const statusMaps = {
            attendance: {
                'normal': '通常出勤',
                'paid_leave': '有給休暇',
                'absent': '欠勤'
            },
            submission: {
                '未提出': '未提出',
                '申請済': '申請済',
                '承認': '承認済',
                '却下': '却下'
            },
            request: {
                '未処理': '未処理',
                '承認': '承認済',
                '却下': '却下'
            },
            employment: {
                'active': '在籍',
                'retired': '退職'
            },
            role: {
                'employee': '一般社員',
                'admin': '管理者'
            }
        };
        
        return statusMaps[type]?.[status] || status;
    }
    
    /**
     * ステータス用CSSクラス生成
     */
    static getStatusClass(status, type) {
        const classMaps = {
            submission: {
                '未提出': 'status-pending',
                '申請済': 'status-submitted',
                '承認': 'status-approved',
                '却下': 'status-rejected'
            },
            request: {
                '未処理': 'status-pending',
                '承認': 'status-approved',
                '却下': 'status-rejected'
            },
            employment: {
                'active': 'status-active',
                'retired': 'status-retired'
            }
        };
        
        return classMaps[type]?.[status] || 'status-default';
    }
    
    /**
     * 数値のカンマ区切り
     */
    static formatNumber(num) {
        if (typeof num !== 'number') return '0';
        return num.toLocaleString('ja-JP');
    }
    
    /**
     * 勤怠集計データフォーマット
     */
    static formatAttendanceSummary(summary) {
        if (!summary) return {};
        
        return {
            totalWorking: this.formatMinutesToHHMM(summary.totalWorkingMinutes),
            totalOvertime: this.formatMinutesToHHMM(summary.totalOvertimeMinutes),
            totalNightShift: this.formatMinutesToHHMM(summary.totalNightShiftMinutes),
            totalLate: this.formatMinutesToHHMM(summary.totalLateMinutes),
            totalEarlyLeave: this.formatMinutesToHHMM(summary.totalEarlyLeaveMinutes),
            paidLeaveDays: `${summary.paidLeaveDays}日`,
            absentDays: `${summary.absentDays}日`
        };
    }
    
    /**
     * テーブル表示用勤怠データフォーマット
     */
    static formatAttendanceRecord(record) {
        if (!record) return {};
        
        return {
            date: DateUtil.formatDate(record.attendanceDate),
            clockIn: record.clockInTime ? DateUtil.formatTimeOnly(record.clockInTime) : '-',
            clockOut: record.clockOutTime ? DateUtil.formatTimeOnly(record.clockOutTime) : '-',
            late: this.formatMinutesToHHMM(record.lateMinutes),
            earlyLeave: this.formatMinutesToHHMM(record.earlyLeaveMinutes),
            overtime: this.formatMinutesToHHMM(record.overtimeMinutes),
            nightShift: this.formatMinutesToHHMM(record.nightShiftMinutes),
            status: record.attendanceFixedFlag ? '確定' : this.formatStatus(record.submissionStatus, 'submission'),
            statusClass: record.attendanceFixedFlag ? 'status-fixed' : this.getStatusClass(record.submissionStatus, 'submission')
        };
    }
    
    /**
     * 社員情報フォーマット
     */
    static formatEmployee(employee) {
        if (!employee) return {};
        
        return {
            id: employee.employeeId,
            code: employee.employeeCode,
            name: employee.employeeName,
            email: employee.email,
            role: this.formatStatus(employee.employeeRole, 'role'),
            status: this.formatStatus(employee.employmentStatus, 'employment'),
            statusClass: this.getStatusClass(employee.employmentStatus, 'employment'),
            hiredAt: DateUtil.formatDate(employee.hiredAt),
            retiredAt: employee.retiredAt ? DateUtil.formatDate(employee.retiredAt) : '-',
            paidLeaveRemainingDays: `${employee.paidLeaveRemainingDays}日`
        };
    }
    
    /**
     * エラーメッセージフォーマット
     */
    static formatErrorMessage(error) {
        if (!error) return 'エラーが発生しました';
        
        // APIエラーレスポンスの場合
        if (error.errorCode && CONFIG.ERROR_MESSAGES[error.errorCode]) {
            return CONFIG.ERROR_MESSAGES[error.errorCode];
        }
        
        // HTTPステータスコードによる判定
        if (error.status) {
            switch (error.status) {
                case 401:
                    return 'セッションがタイムアウトしました';
                case 403:
                    return 'アクセス権限がありません';
                case 404:
                    return '要求されたデータが見つかりません';
                case 500:
                    return 'システムエラーが発生しました';
                default:
                    return error.message || 'エラーが発生しました';
            }
        }
        
        return error.message || 'エラーが発生しました';
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
     * パーセンテージフォーマット
     */
    static formatPercentage(value, total) {
        if (!total || total === 0) return '0%';
        
        const percentage = (value / total) * 100;
        return `${Math.round(percentage)}%`;
    }
    
    /**
     * 電話番号フォーマット
     */
    static formatPhoneNumber(phone) {
        if (!phone) return '';
        
        const cleaned = phone.replace(/\D/g, '');
        const match = cleaned.match(/^(\d{3})(\d{4})(\d{4})$/);
        
        if (match) {
            return `${match[1]}-${match[2]}-${match[3]}`;
        }
        
        return phone;
    }
    
    /**
     * 郵便番号フォーマット
     */
    static formatPostalCode(postal) {
        if (!postal) return '';
        
        const cleaned = postal.replace(/\D/g, '');
        const match = cleaned.match(/^(\d{3})(\d{4})$/);
        
        if (match) {
            return `${match[1]}-${match[2]}`;
        }
        
        return postal;
    }
    
    /**
     * HTMLエスケープ
     */
    static escapeHtml(text) {
        if (typeof text !== 'string') return text;
        
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    /**
     * 改行をBRタグに変換
     */
    static nl2br(text) {
        if (typeof text !== 'string') return text;
        
        return this.escapeHtml(text).replace(/\n/g, '<br>');
    }
    
    /**
     * 文字列切り詰め
     */
    static truncate(text, length = 50, suffix = '...') {
        if (typeof text !== 'string') return '';
        
        if (text.length <= length) return text;
        
        return text.substring(0, length - suffix.length) + suffix;
    }
    
    /**
     * マスク処理（個人情報保護）
     */
    static maskPersonalInfo(text, type = 'default') {
        if (typeof text !== 'string') return text;
        
        switch (type) {
            case 'email':
                const [local, domain] = text.split('@');
                if (domain) {
                    const maskedLocal = local.length > 2 ? 
                        local.substring(0, 2) + '*'.repeat(local.length - 2) : 
                        local;
                    return `${maskedLocal}@${domain}`;
                }
                break;
                
            case 'phone':
                return text.replace(/(\d{3})\d{4}(\d{4})/, '$1-****-$2');
                
            case 'name':
                return text.length > 1 ? 
                    text[0] + '*'.repeat(text.length - 1) : 
                    text;
                
            default:
                return text.replace(/./g, '*');
        }
        
        return text;
    }
}

// グローバルに公開
window.Formatter = Formatter;