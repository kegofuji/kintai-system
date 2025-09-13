// ログイン画面コンポーネント（設計書画面ID L001完全準拠）
class Login {
    constructor(container, app) {
        this.container = container;
        this.app = app;
        this.authService = new AuthService();
    }
    
    render() {
        this.container.innerHTML = `
            <div class="login-container">
                <div class="login-card">
                    <h1 class="login-title">勤怠管理システム</h1>
                    <form id="loginForm">
                        <div class="form-floating mb-3">
                            <input type="text" class="form-control" id="employee_code" 
                                   placeholder="社員ID" maxlength="10" required>
                            <label for="employee_code">社員ID</label>
                            <div class="invalid-feedback"></div>
                        </div>
                        <div class="form-floating mb-3">
                            <input type="password" class="form-control" id="password" 
                                   placeholder="パスワード" maxlength="20" required>
                            <label for="password">パスワード</label>
                            <div class="invalid-feedback"></div>
                        </div>
                        <button type="submit" id="login_btn" class="btn btn-login">
                            ログイン
                        </button>
                    </form>
                </div>
            </div>
        `;
        
        this.attachEventListeners();
    }
    
    attachEventListeners() {
        const form = document.getElementById('loginForm');
        const employeeCodeInput = document.getElementById('employee_code');
        const passwordInput = document.getElementById('password');
        
        // リアルタイムバリデーション
        employeeCodeInput.addEventListener('blur', (e) => {
            this.validateEmployeeCode(e.target.value);
        });
        
        passwordInput.addEventListener('blur', (e) => {
            this.validatePassword(e.target.value, employeeCodeInput.value);
        });
        
        // ログインフォーム送信
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.handleLogin();
        });
    }
    
    validateEmployeeCode(value) {
        const input = document.getElementById('employee_code');
        const feedback = input.nextElementSibling.nextElementSibling;
        
        const result = Validator.validateEmployeeCode(value);
        
        if (result.valid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            feedback.textContent = '';
        } else {
            input.classList.remove('is-valid');
            input.classList.add('is-invalid');
            feedback.textContent = result.message;
        }
        
        return result.valid;
    }
    
    validatePassword(value, employeeCode) {
        const input = document.getElementById('password');
        const feedback = input.nextElementSibling.nextElementSibling;
        
        const result = Validator.validatePassword(value, employeeCode);
        
        if (result.valid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            feedback.textContent = '';
        } else {
            input.classList.remove('is-valid');
            input.classList.add('is-invalid');
            feedback.textContent = result.message;
        }
        
        return result.valid;
    }
    
    async handleLogin() {
        const employeeCode = document.getElementById('employee_code').value.trim();
        const password = document.getElementById('password').value;
        
        // バリデーション実行
        const isValidCode = this.validateEmployeeCode(employeeCode);
        const isValidPassword = this.validatePassword(password, employeeCode);
        
        if (!isValidCode || !isValidPassword) {
            return;
        }
        
        try {
            this.app.showLoading();
            
            const response = await this.authService.login(employeeCode, password);
            
            if (response.success) {
                await this.app.onLoginSuccess(response.data);
            } else {
                this.app.showError(Formatter.formatErrorMessage(response.errorCode, response.message));
            }
            
        } catch (error) {
            this.app.showError(Formatter.formatErrorMessage(error.message));
        } finally {
            this.app.hideLoading();
        }
    }
}

// グローバルに公開
window.Login = Login;