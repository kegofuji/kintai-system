/**
 * バリデーションユーティリティ（バリデーション要件）
 */

class Validator {
    /**
     * 社員コード検証（3-10文字の半角英数字）
     */
    static validateEmployeeCode(employeeCode) {
        if (!employeeCode) return false;
        return CONFIG.VALIDATION.EMPLOYEE_CODE.PATTERN.test(employeeCode) &&
               employeeCode.length >= CONFIG.VALIDATION.EMPLOYEE_CODE.MIN_LENGTH &&
               employeeCode.length <= CONFIG.VALIDATION.EMPLOYEE_CODE.MAX_LENGTH;
    }
    
    /**
     * パスワード検証（要件）
     * 8-20文字、英字（大文字・小文字）、数字、記号を各1文字以上含む
     */
    static validatePassword(password, employeeCode = '') {
        if (!password) return { valid: false, message: 'パスワードは必須です' };
        
        if (password.length < CONFIG.VALIDATION.PASSWORD.MIN_LENGTH) {
            return { valid: false, message: 'パスワードは8文字以上で入力してください' };
        }
        
        if (password.length > CONFIG.VALIDATION.PASSWORD.MAX_LENGTH) {
            return { valid: false, message: 'パスワードは20文字以下で入力してください' };
        }
        
        // 英大文字・小文字・数字・記号チェック
        const hasUpperCase = /[A-Z]/.test(password);
        const hasLowerCase = /[a-z]/.test(password);
        const hasDigit = /[0-9]/.test(password);
        const hasSymbol = /[^a-zA-Z0-9]/.test(password);
        
        if (!hasUpperCase) {
            return { valid: false, message: 'パスワードに英大文字を含めてください' };
        }
        if (!hasLowerCase) {
            return { valid: false, message: 'パスワードに英小文字を含めてください' };
        }
        if (!hasDigit) {
            return { valid: false, message: 'パスワードに数字を含めてください' };
        }
        if (!hasSymbol) {
            return { valid: false, message: 'パスワードに記号を含めてください' };
        }
        
        // 連続する同一文字3文字以上禁止
        if (this.hasRepeatingCharacters(password, 3)) {
            return { valid: false, message: '同一文字を3文字以上連続して使用できません' };
        }
        
        // 社員IDと同一文字列禁止
        if (employeeCode && password.includes(employeeCode)) {
            return { valid: false, message: 'パスワードに社員IDを含めることはできません' };
        }
        
        // よくあるパスワードチェック
        if (this.isCommonPassword(password)) {
            return { valid: false, message: '一般的すぎるパスワードは使用できません' };
        }
        
        return { valid: true, message: '' };
    }
    
    /**
     * 連続する同一文字チェック
     */
    static hasRepeatingCharacters(password, maxRepeat) {
        for (let i = 0; i <= password.length - maxRepeat; i++) {
            const char = password.charAt(i);
            let count = 1;
            
            for (let j = i + 1; j < password.length && password.charAt(j) === char; j++) {
                count++;
                if (count >= maxRepeat) return true;
            }
        }
        return false;
    }
    
    /**
     * よくあるパスワードチェック
     */
    static isCommonPassword(password) {
        const commonPasswords = [
            'password', 'Password', 'password1', 'Password1',
            '12345678', '123456789', 'qwerty', 'Qwerty1',
            'admin', 'Admin123', 'user', 'User123'
        ];
        return commonPasswords.includes(password);
    }
    
    /**
     * メールアドレス検証
     */
    static validateEmail(email) {
        if (!email) return { valid: false, message: 'メールアドレスは必須です' };
        
        if (!CONFIG.VALIDATION.EMAIL.PATTERN.test(email)) {
            return { valid: false, message: '正しいメールアドレスを入力してください' };
        }
        
        return { valid: true, message: '' };
    }
    
    /**
     * 氏名検証
     */
    static validateName(name) {
        if (!name || name.trim() === '') {
            return { valid: false, message: '氏名は必須です' };
        }
        
        if (name.length > CONFIG.VALIDATION.NAME.MAX_LENGTH) {
            return { valid: false, message: `氏名は${CONFIG.VALIDATION.NAME.MAX_LENGTH}文字以内で入力してください` };
        }
        
        return { valid: true, message: '' };
    }
    
    /**
     * 理由・コメント検証
     */
    static validateReason(reason) {
        if (!reason || reason.trim() === '') {
            return { valid: false, message: '理由は必須です' };
        }
        
        if (reason.length > CONFIG.VALIDATION.REASON.MAX_LENGTH) {
            return { valid: false, message: `理由は${CONFIG.VALIDATION.REASON.MAX_LENGTH}文字以内で入力してください` };
        }
        
        return { valid: true, message: '' };
    }
    
    /**
     * 日付検証
     */
    static validateDate(dateString, options = {}) {
        if (!dateString) {
            return { valid: false, message: '日付は必須です' };
        }
        
        if (!DateUtil.isValidDate(dateString)) {
            return { valid: false, message: '正しい日付を入力してください' };
        }
        
        // 未来日チェック
        if (options.futureOnly && !DateUtil.isFutureDate(dateString)) {
            return { valid: false, message: '明日以降の日付を選択してください' };
        }
        
        // 過去日チェック
        if (options.pastOnly && DateUtil.isFutureDate(dateString)) {
            return { valid: false, message: '当日または過去日を選択してください' };
        }
        
        // 営業日チェック
        if (options.workingDayOnly && !DateUtil.isWorkingDay(dateString)) {
            return { valid: false, message: '営業日を選択してください' };
        }
        
        return { valid: true, message: '' };
    }
    
    /**
     * 時刻検証
     */
    static validateTime(timeString) {
        if (!timeString) {
            return { valid: false, message: '時刻は必須です' };
        }
        
        const timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;
        if (!timePattern.test(timeString)) {
            return { valid: false, message: '正しい時刻を入力してください（HH:MM形式）' };
        }
        
        return { valid: true, message: '' };
    }
    
    /**
     * 年月検証
     */
    static validateYearMonth(yearMonthString) {
        if (!yearMonthString) {
            return { valid: false, message: '年月は必須です' };
        }
        
        if (!/^\d{4}-\d{2}$/.test(yearMonthString)) {
            return { valid: false, message: '年月はYYYY-MM形式で入力してください' };
        }
        
        const [year, month] = yearMonthString.split('-').map(Number);
        
        if (year < 2020 || year > 2030) {
            return { valid: false, message: '年は2020-2030の範囲で入力してください' };
        }
        
        if (month < 1 || month > 12) {
            return { valid: false, message: '月は01-12の範囲で入力してください' };
        }
        
        return { valid: true, message: '' };
    }
    
    /**
     * 数値検証
     */
    static validateNumber(value, options = {}) {
        if (value === '' || value === null || value === undefined) {
            return { valid: false, message: '数値は必須です' };
        }
        
        const num = Number(value);
        if (isNaN(num)) {
            return { valid: false, message: '正しい数値を入力してください' };
        }
        
        if (options.min !== undefined && num < options.min) {
            return { valid: false, message: `${options.min}以上の値を入力してください` };
        }
        
        if (options.max !== undefined && num > options.max) {
            return { valid: false, message: `${options.max}以下の値を入力してください` };
        }
        
        if (options.integer && !Number.isInteger(num)) {
            return { valid: false, message: '整数を入力してください' };
        }
        
        return { valid: true, message: '' };
    }
    
    /**
     * フォーム一括検証
     */
    static validateForm(formElement, rules) {
        const errors = {};
        let isValid = true;
        
        for (const fieldName in rules) {
            const field = formElement.querySelector(`[name="${fieldName}"]`);
            if (!field) continue;
            
            const rule = rules[fieldName];
            const value = field.value;
            
            let result = { valid: true, message: '' };
            
            // 必須チェック
            if (rule.required && (!value || value.trim() === '')) {
                result = { valid: false, message: `${rule.label || fieldName}は必須です` };
            }
            // カスタム検証
            else if (rule.validator && typeof rule.validator === 'function') {
                result = rule.validator(value);
            }
            
            if (!result.valid) {
                errors[fieldName] = result.message;
                isValid = false;
                
                // エラー表示
                field.classList.add('error');
                const errorElement = formElement.querySelector(`#${fieldName}-error`);
                if (errorElement) {
                    errorElement.textContent = result.message;
                }
            } else {
                // エラークリア
                field.classList.remove('error');
                const errorElement = formElement.querySelector(`#${fieldName}-error`);
                if (errorElement) {
                    errorElement.textContent = '';
                }
            }
        }
        
        return { valid: isValid, errors };
    }
    
    /**
     * リアルタイム検証セットアップ
     */
    static setupRealTimeValidation(formElement, rules) {
        for (const fieldName in rules) {
            const field = formElement.querySelector(`[name="${fieldName}"]`);
            if (!field) continue;
            
            const rule = rules[fieldName];
            
            // 入力時検証
            field.addEventListener('input', () => {
                const result = rule.validator ? rule.validator(field.value) : { valid: true, message: '' };
                
                if (!result.valid) {
                    field.classList.add('error');
                    const errorElement = formElement.querySelector(`#${fieldName}-error`);
                    if (errorElement) {
                        errorElement.textContent = result.message;
                    }
                } else {
                    field.classList.remove('error');
                    const errorElement = formElement.querySelector(`#${fieldName}-error`);
                    if (errorElement) {
                        errorElement.textContent = '';
                    }
                }
            });
            
            // フォーカス離脱時検証
            field.addEventListener('blur', () => {
                if (rule.required && (!field.value || field.value.trim() === '')) {
                    field.classList.add('error');
                    const errorElement = formElement.querySelector(`#${fieldName}-error`);
                    if (errorElement) {
                        errorElement.textContent = `${rule.label || fieldName}は必須です`;
                    }
                }
            });
        }
    }
}

// グローバルに公開
window.Validator = Validator;