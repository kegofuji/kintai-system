-- データベースのキャラクターセット設定
ALTER DATABASE kintai_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 社員テーブル
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_code VARCHAR(10) UNIQUE NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ROLE_ADMIN', 'ROLE_EMPLOYEE') NOT NULL,
    status ENUM('ACTIVE', 'RETIRED') NOT NULL DEFAULT 'ACTIVE',
    hired_at DATE NOT NULL,
    retired_at DATE,
    paid_leave_days DECIMAL(4,1) NOT NULL DEFAULT 10.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 勤怠記録テーブル
CREATE TABLE attendance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    clock_in_time TIMESTAMP,
    clock_out_time TIMESTAMP,
    status ENUM('NORMAL', 'PAID_LEAVE', 'ABSENT') NOT NULL DEFAULT 'NORMAL',
    late_minutes INT DEFAULT 0,
    early_leave_minutes INT DEFAULT 0,
    overtime_minutes INT DEFAULT 0,
    night_shift_minutes INT DEFAULT 0,
    break_minutes INT DEFAULT 60,
    note TEXT,
    is_fixed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    UNIQUE KEY uk_employee_date (employee_id, attendance_date)
);

-- 申請テーブル
CREATE TABLE requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    request_type ENUM('LEAVE', 'ADJUSTMENT') NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    target_date DATE NOT NULL,
    reason TEXT NOT NULL,
    corrected_clock_in_time TIMESTAMP,
    corrected_clock_out_time TIMESTAMP,
    approver_id BIGINT,
    approved_at TIMESTAMP,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (approver_id) REFERENCES employees(id)
);

-- 月次申請テーブル
CREATE TABLE monthly_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    target_year_month VARCHAR(7) NOT NULL,
    status ENUM('NOT_SUBMITTED', 'SUBMITTED', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'NOT_SUBMITTED',
    submitted_at TIMESTAMP,
    approver_id BIGINT,
    approved_at TIMESTAMP,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (approver_id) REFERENCES employees(id),
    UNIQUE KEY uk_employee_yearmonth (employee_id, target_year_month)
);

-- 管理者アカウントの作成
INSERT INTO employees (
    employee_code,
    employee_name,
    email,
    password_hash,
    role,
    status,
    hired_at
) VALUES (
    'admin',
    '管理者',
    'admin@example.com',
    '$2a$10$rJXX.hVhQejx2h8xjMQ8A.GNS.v.7YE8bliP9AEHVXWdhPD5dSOdC', -- パスワード: Admin123!
    'ROLE_ADMIN',
    'ACTIVE',
    CURRENT_DATE
);
