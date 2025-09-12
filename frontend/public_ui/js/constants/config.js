/**
 * アプリケーション設定（設定値）
 * 勤怠管理システム - フロントエンド設定ファイル
 */

const CONFIG = {
    // API設定
    API_BASE_URL: window.location.hostname === 'localhost' ? 
        'http://localhost:8080/api' : 
        `${window.location.protocol}//${window.location.host}/api`,
    
    // FastAPI設定（PDFレポート生成用）
    FASTAPI_URL: window.location.hostname === 'localhost' ? 
        'http://localhost:8081' : 
        `${window.location.protocol}//${window.location.host.replace(':8080', ':8081')}`,
    
    // セッション設定（10分タイムアウト）
    SESSION_TIMEOUT_MINUTES: 10,
    SESSION_WARNING_MINUTES: 9, // 残り1分で警告
    
    // 勤怠管理設定（勤務時間）
    WORK_TIME: {
        STANDARD_START: '09:00',
        STANDARD_END: '18:00',
        LUNCH_START: '12:00',
        LUNCH_END: '13:00',
        NIGHT_START: '22:00',
        NIGHT_END: '05:00',
        STANDARD_WORKING_HOURS: 8
    },
    
    // バリデーション設定
    VALIDATION: {
        EMPLOYEE_CODE: {
            MIN_LENGTH: 3,
            MAX_LENGTH: 10,
            PATTERN: /^[a-zA-Z0-9]+$/
        },
        PASSWORD: {
            MIN_LENGTH: 8,
            MAX_LENGTH: 20,
            PATTERN: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z\d]).{8,20}$/
        },
        EMAIL: {
            PATTERN: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
        },
        NAME: {
            MAX_LENGTH: 50
        },
        REASON: {
            MAX_LENGTH: 200
        }
    },
    
    // ページネーション設定
    PAGINATION: {
        DEFAULT_PAGE_SIZE: 20,
        MAX_PAGE_SIZE: 100
    },
    
    // メッセージ表示設定
    MESSAGE: {
        DURATION: {
            SUCCESS: 3000,
            INFO: 5000,
            WARNING: 7000,
            ERROR: 10000
        }
    },
    
    // 日付・時刻フォーマット
    DATE_FORMAT: {
        DISPLAY: 'YYYY/MM/DD',
        INPUT: 'YYYY-MM-DD',
        TIME: 'HH:mm',
        DATETIME: 'YYYY/MM/DD HH:mm'
    },
    
    // ローカルストレージキー
    STORAGE_KEYS: {
        USER_PREFERENCES: 'kintai_user_preferences',
        TEMP_DATA: 'kintai_temp_data'
    },
    
    // UI設定
    UI: {
        ANIMATION_DURATION: 300,
        DEBOUNCE_DELAY: 500,
        AUTO_REFRESH_INTERVAL: 30000 // 30秒
    },
    
    // エラーメッセージ（エラーコード対応）
    ERROR_MESSAGES: {
        // 認証関連
        'AUTH_FAILED': '社員IDまたはパスワードが正しくありません',
        'SESSION_TIMEOUT': 'セッションがタイムアウトしました',
        'ACCESS_DENIED': 'アクセス権限がありません',
        
        // 勤怠関連
        'ALREADY_CLOCKED_IN': '既に出勤打刻済みです',
        'NOT_CLOCKED_IN': '出勤打刻が必要です',
        'INCOMPLETE_ATTENDANCE': '打刻漏れがあります',
        'FIXED_ATTENDANCE': '確定済のため変更できません',
        
        // 申請関連
        'INSUFFICIENT_LEAVE_DAYS': '有給残日数が不足しています',
        'DUPLICATE_REQUEST': '既に申請済みです',
        'INVALID_DATE': '申請日が正しくありません',
        
        // 社員管理関連
        'EMPLOYEE_NOT_FOUND': '社員が見つかりません',
        'DUPLICATE_CODE': '社員コードが既に使用されています',
        'DUPLICATE_EMAIL': 'メールアドレスが既に使用されています',
        
        // システム関連
        'VALIDATION_ERROR': '入力内容に誤りがあります',
        'SYSTEM_ERROR': 'システムエラーが発生しました',
        'NETWORK_ERROR': 'ネットワークエラーが発生しました'
    },
    
    // 成功メッセージ
    SUCCESS_MESSAGES: {
        LOGIN: 'ログインしました',
        LOGOUT: 'ログアウトしました',
        CLOCK_IN: '出勤打刻が完了しました',
        CLOCK_OUT: '退勤打刻が完了しました',
        LEAVE_REQUEST: '有給申請が完了しました',
        ADJUSTMENT_REQUEST: '打刻修正申請が完了しました',
        MONTHLY_SUBMIT: '月末申請が完了しました',
        APPROVE: '承認しました',
        REJECT: '却下しました',
        SAVE: '保存しました',
        DELETE: '削除しました',
        UPDATE: '更新しました'
    },
    
    // ステータス定義
    STATUS: {
        ATTENDANCE: {
            NORMAL: 'normal',
            PAID_LEAVE: 'paid_leave',
            ABSENT: 'absent'
        },
        SUBMISSION: {
            NOT_SUBMITTED: '未提出',
            SUBMITTED: '申請済',
            APPROVED: '承認',
            REJECTED: '却下'
        },
        REQUEST: {
            PENDING: '未処理',
            APPROVED: '承認',
            REJECTED: '却下'
        },
        EMPLOYMENT: {
            ACTIVE: 'active',
            RETIRED: 'retired'
        },
        ROLE: {
            EMPLOYEE: 'employee',
            ADMIN: 'admin'
        }
    },
    
    // 色設定（CSSカスタムプロパティと連動）
    COLORS: {
        PRIMARY: '#0066cc',
        SUCCESS: '#28a745',
        DANGER: '#dc3545',
        WARNING: '#ffc107',
        INFO: '#17a2b8',
        LIGHT: '#f8f9fa',
        DARK: '#343a40'
    },
    
    // HTTPリクエスト設定
    HTTP: {
        TIMEOUT: 30000, // 30秒
        RETRY_COUNT: 3,
        RETRY_DELAY: 1000
    },
    
    // デバッグ設定
    DEBUG_MODE: window.location.hostname === 'localhost' || window.location.search.includes('debug=1'),
    
    // PWA設定
    PWA: {
        ENABLE_SERVICE_WORKER: window.location.protocol === 'https:',
        UPDATE_CHECK_INTERVAL: 60000 // 1分
    },
    
    // レポート設定
    REPORT: {
        PDF_TIMEOUT: 60000, // 60秒
        MAX_FILE_SIZE: 10 * 1024 * 1024 // 10MB
    }
};

// 環境別設定オーバーライド
if (window.location.hostname !== 'localhost') {
    // 本番環境設定
    CONFIG.DEBUG_MODE = false;
    CONFIG.HTTP.TIMEOUT = 60000; // 本番では60秒
}

// 設定値の凍結（変更防止）
Object.freeze(CONFIG);

// デバッグ情報出力
if (CONFIG.DEBUG_MODE) {
    console.log('勤怠管理システム設定:', CONFIG);
}