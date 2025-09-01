export function validateEmail(email) {
    const re = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return re.test(email);
}

export function validatePassword(password) {
    // 最低8文字、大文字小文字、数字を含む
    const re = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/;
    return re.test(password);
}

export function validateDateRange(startDate, endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);
    return start <= end;
}

export function validateRequired(value) {
    return value !== null && value !== undefined && value.trim() !== '';
}

export function validateForm(formData, schema) {
    const errors = {};
    
    for (const [field, rules] of Object.entries(schema)) {
        const value = formData[field];
        
        if (rules.required && !validateRequired(value)) {
            errors[field] = `${field}は必須です`;
            continue;
        }
        
        if (rules.email && !validateEmail(value)) {
            errors[field] = '有効なメールアドレスを入力してください';
        }
        
        if (rules.password && !validatePassword(value)) {
            errors[field] = 'パスワードは8文字以上で、大文字、小文字、数字を含める必要があります';
        }
        
        if (rules.minLength && value.length < rules.minLength) {
            errors[field] = `${field}は${rules.minLength}文字以上である必要があります`;
        }
        
        if (rules.maxLength && value.length > rules.maxLength) {
            errors[field] = `${field}は${rules.maxLength}文字以下である必要があります`;
        }
    }
    
    return {
        isValid: Object.keys(errors).length === 0,
        errors
    };
}
