// API設定
export const API_BASE_URL = 'http://localhost:8080/api';
export const API_TIMEOUT = 5000;

// 認証関連
export const TOKEN_KEY = 'auth_token';
export const REFRESH_TOKEN_KEY = 'refresh_token';
export const AUTH_HEADER = 'Authorization';

// 勤怠状態
export const ATTENDANCE_STATUS = {
    WORKING: 'WORKING',
    OFF_DUTY: 'OFF_DUTY',
    ON_BREAK: 'ON_BREAK',
    ON_LEAVE: 'ON_LEAVE'
};

// 申請状態
export const REQUEST_STATUS = {
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED'
};

// 従業員種別
export const EMPLOYEE_TYPES = {
    REGULAR: '正社員',
    CONTRACT: '契約社員',
    PART_TIME: 'パートタイム',
    TEMPORARY: '派遣社員'
};

// 部署コード
export const DEPARTMENT_CODES = {
    SALES: '営業部',
    ENGINEERING: '技術部',
    HUMAN_RESOURCES: '人事部',
    ACCOUNTING: '経理部',
    GENERAL_AFFAIRS: '総務部'
};

// 休暇種別
export const LEAVE_TYPES = {
    PAID: '有給休暇',
    SICK: '病気休暇',
    PERSONAL: '私用休暇',
    SPECIAL: '特別休暇'
};

// ページネーション設定
export const PAGINATION = {
    DEFAULT_PAGE_SIZE: 10,
    MAX_PAGE_SIZE: 100
};

// 日付フォーマット
export const DATE_FORMATS = {
    DISPLAY: 'YYYY年MM月DD日',
    INPUT: 'YYYY-MM-DD',
    API: 'YYYY-MM-DD'
};

// 時間設定
export const TIME_CONFIG = {
    WORK_START: '09:00',
    WORK_END: '18:00',
    BREAK_START: '12:00',
    BREAK_END: '13:00',
    WORK_HOURS_PER_DAY: 8
};

// エラーメッセージ
export const ERROR_MESSAGES = {
    NETWORK_ERROR: 'ネットワークエラーが発生しました',
    AUTH_ERROR: '認証エラーが発生しました',
    VALIDATION_ERROR: '入力内容に誤りがあります',
    SERVER_ERROR: 'サーバーエラーが発生しました',
    NOT_FOUND: '要求されたリソースが見つかりません'
};
