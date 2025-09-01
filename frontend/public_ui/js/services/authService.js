import { API_BASE_URL } from '../constants/config.js';

export const authService = {
    async login(username, password) {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });
            return await response.json();
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    },

    async logout() {
        // ログアウト処理
        localStorage.removeItem('token');
        window.location.href = '/login';
    }
};

export function setupAuthListeners() {
    const loginForm = document.querySelector('#login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = loginForm.username.value;
            const password = loginForm.password.value;
            try {
                const result = await authService.login(username, password);
                if (result.token) {
                    localStorage.setItem('token', result.token);
                    window.location.href = '/dashboard';
                }
            } catch (error) {
                console.error('Login failed:', error);
            }
        });
    }
}
