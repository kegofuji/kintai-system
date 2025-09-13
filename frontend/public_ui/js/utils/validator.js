// バリデーション機能（設計書のバリデーション仕様完全準拠）
class Validator {
    /**
     * 社員コード検証（設計書：3-10文字半角英数字）
     */
    static validateEmployeeCode(code) {
        if (!code) {
            return { valid: false, message: '社員IDは3-10文字の半角英数字で入力してください' };
        }
        
        if (code.length < 3 || code.length > 10) {
            return { valid: false, message: '社員IDは3-10文字の半角英数字で入力してください' };
        }
        
        if (!/^[a-zA-Z0-9]+$/.test(code)) {
            return { valid: false, message: '社員IDは3-10文字の半角英数字で入力してください' };
        }
        
        return { valid: true };
    }
    
    /**
     * パスワード検証（設計書の詳細ルール完全準拠）
     * - 8-20文字
     * - 英字（大文字・小文字）、数字、記号を各1文字以上含む
     * - 連続する同一文字3文字以上禁止
     * - 社員IDと同一文字列禁止
     */
    static validatePassword(password, employeeCode = '') {
        if (!password) {
            return { valid: false, message: 'パスワードは8文字以上で入力してください' };
        }
        
        if (password.length < 8 || password.length > 20) {
            return { valid: false, message: 'パスワードは8-20文字の英数字記号を含めて入力してください' };
        }
        
        // 英字（大文字・小文字）、数字、記号の各1文字以上チェック
        const hasLowerCase = /[a-z]/.test(password);
        const hasUpperCase = /[A-Z]/.test(password);
        const hasDigit = /\d/.test(password);
        const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\?]/.test(password);
        
        if (!hasLowerCase || !hasUpperCase || !hasDigit || !hasSpecialChar) {
            return { valid: false, message: 'パスワードは英字（大文字・小文字）、数字、記号を各1文字以上含めて入力してください' };
        }
        
        // 連続する同一文字3文字以上禁止
        for (let i = 0; i < password.length - 2; i++) {
            if (password.charAt(i) === password.charAt(i + 1) && 
                password.charAt(i + 1) === password.charAt(i + 2)) {
                return { valid: false, message: '連続する同一文字3文字以上は使用できません' };
            }
        }
        
        // 社員IDと同一文字列禁止
        if (employeeCode && password.toLowerCase() === employeeCode.toLowerCase()) {
            return { valid: false, message: '社員IDと同一のパスワードは使用できません' };
        }
        
        return { valid: true };
    }
    
    /**
     * メールアドレス検証
     */
    static validateEmail(email) {
        if (!email) {
            return { valid: false, message: '正しいメールアドレスを入力してください' };
        }
        
        const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        if (!emailRegex.test(email)) {
            return { valid: false, message: '正しいメールアドレスを入力してください' };
        }
        
        if (email.length > 100) {
            return { valid: false, message: 'メールアドレスは100文字以内で入力してください' };
        }
        
        return { valid: true };
    }
    
    /**
     * 氏名検証
     */
    static validateEmployeeName(name) {
        if (!name || name.trim().length === 0) {
            return { valid: false, message: '氏名は50文字以内で入力してください' };
        }
        
        if (name.length > 50) {
            return { valid: false, message: '氏名は50文字以内で入力してください' };
        }
        
        return { valid: true };
    }
    
    /**
     * 理由欄検証（設計書：最大200文字）
     */
    static validateReason(reason) {
        if (!reason || reason.trim().length === 0) {
            return { valid: false, message: '理由は200文字以内で入力してください' };
        }
        
        if (reason.length > 200) {
            return { valid: false, message: '理由は200文字以内で入力してください' };
        }
        
        return { valid: true };
    }
    
    /**
     * 有給申請日検証（設計書：未来日のみ）
     */
    static validateLeaveDate(dateString) {
        if (!dateString) {
            return { valid: false, message: '有給取得日は明日以降を選択してください' };
        }
        
        const requestDate = new Date(dateString);
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        tomorrow.setHours(0, 0, 0, 0);
        
        if (requestDate < tomorrow) {
            return { valid: false, message: '有給取得日は明日以降を選択してください' };
        }
        
        return { valid: true };
    }
    
    /**
     * 打刻修正申請日検証（設計書：当日または過去日のみ）
     */
    static validateAdjustmentDate(dateString) {
        if (!dateString) {
            return { valid: false, message: '修正対象日は当日または過去日を選択してください' };
        }
        
        const targetDate = new Date(dateString);
        const today = new Date();
        today.setHours(23, 59, 59, 999);
        
        if (targetDate > today) {
            return { valid: false, message: '修正対象日は当日または過去日を選択してください' };
        }
        
        return { valid: true };
    }
    
    /**
     * 時刻検証（HH:MM形式、00:00-23:59）
     */
    static validateTime(timeString) {
        if (!timeString) {
            return { valid: false, message: '時刻は00:00-23:59で入力してください' };
        }
        
        const timeRegex = /^([01]?\d|2[0-3]):([0-5]?\d)$/;
        if (!timeRegex.test(timeString)) {
            return { valid: false, message: '時刻は00:00-23:59で入力してください' };
        }
        
        return { valid: true };
    }
    
    /**
     * 有給日数調整検証（設計書：-99～+99）
     */
    static validateAdjustmentDays(days) {
        const numDays = parseInt(days);
        
        if (isNaN(numDays)) {
            return { valid: false, message: '調整日数は-99～+99の範囲で入力してください' };
        }
        
        if (numDays < -99 || numDays > 99) {
            return { valid: false, message: '調整日数は-99～+99の範囲で入力してください' };
        }
        
        return { valid: true };
    }
    
    /**
     * 必須チェック（互換性のため）
     */
    static required(value, fieldName) {
        if (!value || value.trim().length === 0) {
            return { valid: false, message: `${fieldName}は必須です` };
        }
        return { valid: true };
    }

    /**
     * 文字数チェック（互換性のため）
     */
    static length(value, min, max, fieldName) {
        if (!value) {
            return { valid: true }; // 必須チェックは別途実行
        }
        
        if (value.length < min) {
            return { valid: false, message: `${fieldName}は${min}文字以上で入力してください` };
        }
        
        if (value.length > max) {
            return { valid: false, message: `${fieldName}は${max}文字以内で入力してください` };
        }
        
        return { valid: true };
    }

    /**
     * メールアドレスチェック（互換性のため）
     */
    static email(email) {
        return this.validateEmail(email);
    }

    /**
     * 数値チェック（互換性のため）
     */
    static number(value, min, max, fieldName) {
        if (!value) {
            return { valid: true }; // 必須チェックは別途実行
        }
        
        const num = parseFloat(value);
        if (isNaN(num)) {
            return { valid: false, message: `${fieldName}は数値で入力してください` };
        }
        
        if (num < min) {
            return { valid: false, message: `${fieldName}は${min}以上で入力してください` };
        }
        
        if (num > max) {
            return { valid: false, message: `${fieldName}は${max}以下で入力してください` };
        }
        
        return { valid: true };
    }

    /**
     * 日付チェック（互換性のため）
     */
    static date(dateString, fieldName) {
        if (!dateString) {
            return { valid: true }; // 必須チェックは別途実行
        }
        
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return { valid: false, message: `${fieldName}は正しい日付を入力してください` };
        }
        
        return { valid: true };
    }

    /**
     * フォーム全体のバリデーション
     */
    static validateForm(form, rules) {
        const errors = {};
        let hasError = false;

        for (const [fieldName, validators] of Object.entries(rules)) {
            const input = form.querySelector(`[name="${fieldName}"]`);
            if (!input) continue;

            const value = input.value;
            
            for (const validator of validators) {
                const result = validator.validator(...validator.params);
                if (!result.valid) {
                    errors[fieldName] = result.message;
                    hasError = true;
                    break;
                }
            }
        }

        return { valid: !hasError, errors };
    }
}

// グローバルに公開
window.Validator = Validator;