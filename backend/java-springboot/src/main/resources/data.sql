-- 初期データ投入用SQLファイル
-- パスワードハッシュは BCrypt で生成済み（パスワード: AdminPass123!）

-- 管理者アカウント
INSERT INTO employees (
    employee_code, employee_name, email, employee_password_hash, employee_role,
    employment_status, hired_at, paid_leave_remaining_days, created_at, updated_at
) VALUES (
    'ADMIN001', 
    '管理者', 
    'admin@company.com',
    '$2a$10$7C6kNWLUgx8xjK9rFiJLJeF.pOmYVjWn7XcU7I5rlYOGTMJlf5Qa.',  -- AdminPass123!
    'ADMIN',
    'ACTIVE', 
    '2025-01-01', 
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON DUPLICATE KEY UPDATE employee_name = VALUES(employee_name);

-- テストユーザーアカウント（パスワード: TestPass123!）
INSERT INTO employees (
    employee_code, employee_name, email, employee_password_hash, employee_role,
    employment_status, hired_at, paid_leave_remaining_days, created_at, updated_at
) VALUES 
(
    'E001', 
    '山田太郎', 
    'yamada@company.com',
    '$2a$10$8D7kOXMVhx9yk0LaGjKMMefQ.qPnZWkXo8dV8J6smZPHUOKmg6Rbm',  -- TestPass123!
    'EMPLOYEE',
    'ACTIVE', 
    '2025-01-01', 
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'E002', 
    '鈴木花子', 
    'suzuki@company.com',
    '$2a$10$8D7kOXMVhx9yk0LaGjKMMefQ.qPnZWkXo8dV8J6smZPHUOKmg6Rbm',  -- TestPass123!
    'EMPLOYEE',
    'ACTIVE', 
    '2025-02-01', 
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'E003', 
    '佐藤次郎', 
    'sato@company.com',
    '$2a$10$8D7kOXMVhx9yk0LaGjKMMefQ.qPnZWkXo8dV8J6smZPHUOKmg6Rbm',  -- TestPass123!
    'EMPLOYEE',
    'RETIRED', 
    '2024-04-01', 
    5,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON DUPLICATE KEY UPDATE employee_name = VALUES(employee_name);

-- 退職者の退職日設定
UPDATE employees 
SET retired_at = '2025-07-31' 
WHERE employee_code = 'E003' AND employment_status = 'RETIRED';

-- テスト用勤怠データ（2025年8月分）
INSERT INTO attendance_records (
    employee_id, attendance_date, clock_in_time, clock_out_time, 
    late_minutes, early_leave_minutes, overtime_minutes, night_shift_minutes,
    attendance_status, submission_status, attendance_fixed_flag, created_at, updated_at
) VALUES
-- 山田太郎（E001）の勤怠データ
(1, '2025-08-01', '2025-08-01 09:05:00', '2025-08-01 18:10:00', 5, 0, 10, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-02', '2025-08-02 09:00:00', '2025-08-02 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-03', '2025-08-03 09:00:00', '2025-08-03 19:30:00', 0, 0, 90, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-05', '2025-08-05 09:15:00', '2025-08-05 18:00:00', 15, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-06', '2025-08-06 09:00:00', '2025-08-06 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-07', '2025-08-07 09:00:00', '2025-08-07 23:00:00', 0, 0, 300, 60, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-08', '2025-08-08 09:10:00', '2025-08-08 17:50:00', 10, 10, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-09', NULL, NULL, 0, 0, 0, 0, 'PAID_LEAVE', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- 鈴木花子（E002）の勤怠データ
(2, '2025-08-01', '2025-08-01 09:00:00', '2025-08-01 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-08-02', '2025-08-02 09:03:00', '2025-08-02 18:15:00', 3, 0, 15, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-08-03', '2025-08-03 09:00:00', '2025-08-03 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-08-05', '2025-08-05 09:00:00', '2025-08-05 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-08-06', '2025-08-06 09:00:00', '2025-08-06 18:30:00', 0, 0, 30, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-08-07', '2025-08-07 09:00:00', '2025-08-07 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-08-08', '2025-08-08 09:00:00', '2025-08-08 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-08-09', '2025-08-09 09:00:00', '2025-08-09 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- テスト用有給申請データ
INSERT INTO leave_requests (
    employee_id, leave_request_date, leave_request_reason, leave_request_status, 
    created_at, updated_at
) VALUES
(1, '2025-09-15', '私用のため', '未処理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-09-20', '家族の用事', '未処理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- テスト用打刻修正申請データ  
INSERT INTO adjustment_requests (
    employee_id, adjustment_target_date, original_clock_in_time, original_clock_out_time,
    adjustment_requested_time_in, adjustment_requested_time_out, adjustment_reason,
    adjustment_status, created_at, updated_at
) VALUES
(1, '2025-08-05', '2025-08-05 09:15:00', '2025-08-05 18:00:00', 
 '2025-08-05 09:00:00', '2025-08-05 18:00:00', '打刻忘れのため', '未処理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-08-02', '2025-08-02 09:03:00', '2025-08-02 18:15:00',
 '2025-08-02 09:00:00', '2025-08-02 18:00:00', 'システム不具合のため', '未処理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- インデックス作成（パフォーマンス向上のため）
CREATE INDEX IF NOT EXISTS idx_employee_code ON employees(employee_code);
CREATE INDEX IF NOT EXISTS idx_employee_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_employee_status ON employees(employment_status);

CREATE INDEX IF NOT EXISTS idx_attendance_employee_date ON attendance_records(employee_id, attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance_records(attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_status ON attendance_records(attendance_status);
CREATE INDEX IF NOT EXISTS idx_submission_status ON attendance_records(submission_status);

CREATE INDEX IF NOT EXISTS idx_leave_employee_date ON leave_requests(employee_id, leave_request_date);
CREATE INDEX IF NOT EXISTS idx_leave_date ON leave_requests(leave_request_date);
CREATE INDEX IF NOT EXISTS idx_leave_status ON leave_requests(leave_request_status);

CREATE INDEX IF NOT EXISTS idx_adjustment_employee_date ON adjustment_requests(employee_id, adjustment_target_date);
CREATE INDEX IF NOT EXISTS idx_adjustment_date ON adjustment_requests(adjustment_target_date);
CREATE INDEX IF NOT EXISTS idx_adjustment_status ON adjustment_requests(adjustment_status);