// アプリケーション設定定数（設計書の技術仕様完全準拠）
const CONFIG = {
    // API設定（設計書：Spring Boot Port 8080, FastAPI Port 8081）
    API_BASE_URL: 'http://localhost:8080/api',
    PDF_API_URL: 'http://localhost:8081/reports',
    
    // 本番環境用（Railway）
    PRODUCTION_API_URL: 'https://kintai-backend.railway.app/api',
    PRODUCTION_PDF_URL: 'https://kintai-pdf.railway.app/reports',
    
    // セッション設定（設計書：10分タイムアウト）
    SESSION_TIMEOUT_MINUTES: 10,
    SESSION_CHECK_INTERVAL_MS: 60000,
    
    // 勤怠設定（設計書仕様）
    WORKING_HOURS: {
        STANDARD_START: '09:00',
        STANDARD_END: '18:00',
        LUNCH_START: '12:00',
        LUNCH_END: '13:00',
        NIGHT_START: '22:00',
        NIGHT_END: '05:00'
    },
    
    // バリデーション設定
    VALIDATION: {
        EMPLOYEE_CODE_MIN: 3,
        EMPLOYEE_CODE_MAX: 10,
        PASSWORD_MIN: 8,
        PASSWORD_MAX: 20,
        REASON_MAX: 200
    },
    
    // 環境判定
    isProduction: () => {
        return window.location.hostname !== 'localhost' && 
               window.location.hostname !== '127.0.0.1';
    },
    
    // API URL取得
    getApiBaseUrl: () => {
        return CONFIG.isProduction() ? CONFIG.PRODUCTION_API_URL : CONFIG.API_BASE_URL;
    },
    
    getPdfApiUrl: () => {
        return CONFIG.isProduction() ? CONFIG.PRODUCTION_PDF_URL : CONFIG.PDF_API_URL;
    },
    
    // 画面ID（設計書準拠）
    SCREEN_IDS: {
        LOGIN: 'L001',
        EMPLOYEE_DASHBOARD: 'E001',
        ATTENDANCE_HISTORY: 'E002',
        LEAVE_REQUEST: 'E003',
        ADJUSTMENT_REQUEST: 'E004',
        ADMIN_DASHBOARD: 'A001',
        EMPLOYEE_MANAGEMENT: 'A002',
        ATTENDANCE_MANAGEMENT: 'A003',
        APPROVAL_MANAGEMENT: 'A004',
        LEAVE_MANAGEMENT: 'A005',
        REPORT_GENERATION: 'A006'
    },
    
    // ユーザーロール
    ROLES: {
        EMPLOYEE: 'employee',
        ADMIN: 'admin'
    },
    
    // 勤怠ステータス
    ATTENDANCE_STATUS: {
        NORMAL: 'normal',
        PAID_LEAVE: 'paid_leave',
        ABSENT: 'absent'
    },
    
    // 申請ステータス
    REQUEST_STATUS: {
        PENDING: 'pending',
        APPROVED: 'approved',
        REJECTED: 'rejected'
    },
    
    // ストレージキー
    STORAGE_KEYS: {
        AUTH_TOKEN: 'authToken',
        USER_INFO: 'userInfo'
    },
    
    // メッセージタイプ
    MESSAGE_TYPES: {
        SUCCESS: 'success',
        ERROR: 'error',
        INFO: 'info',
        WARNING: 'warning'
    },
    
    // 日付フォーマット
    DATE_FORMATS: {
        DISPLAY: 'YYYY/MM/DD',
        API: 'YYYY-MM-DD',
        MONTH: 'YYYY-MM'
    },
    
    // 時間フォーマット
    TIME_FORMATS: {
        DISPLAY: 'HH:mm',
        API: 'HH:mm:ss'
    },
    
    // ページネーション設定
    PAGINATION: {
        DEFAULT_PAGE_SIZE: 20,
        MAX_PAGE_SIZE: 100
    }
};

// グローバルに公開
window.CONFIG = CONFIG;