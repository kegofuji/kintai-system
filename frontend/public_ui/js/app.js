import { initRouter } from './utils/router.js';
import { setupAuthListeners } from './services/authService.js';
import { setupAttendanceListeners } from './services/attendanceService.js';
import { setupRequestListeners } from './services/requestService.js';
import { setupAdminListeners } from './services/adminService.js';

document.addEventListener('DOMContentLoaded', () => {
    // ルーターの初期化
    initRouter();

    // 各機能のイベントリスナーを設定
    setupAuthListeners();
    setupAttendanceListeners();
    setupRequestListeners();
    setupAdminListeners();
});
