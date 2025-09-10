-- 初期データ投入用SQLファイル（設計書通りの実装）
-- パスワードハッシュは BCrypt で生成済み

-- 管理者アカウント（設計書通りの初期データ）
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

-- テストユーザーアカウント（設計書通りの初期データ）
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

-- 退職者の退職日設定（設計書通り）
UPDATE employees 
SET retired_at = '2025-07-31' 
WHERE employee_code = 'E003' AND employment_status = 'RETIRED';

-- テスト用勤怠データ（2025年8月分）- 設計書通りの時間計算
INSERT INTO attendance_records (
    employee_id, attendance_date, clock_in_time, clock_out_time, 
    late_minutes, early_leave_minutes, overtime_minutes, night_shift_minutes,
    attendance_status, submission_status, attendance_fixed_flag, created_at, updated_at
) VALUES
-- 山田太郎（E001）の勤怠データ
(1, '2025-08-01', '2025-08-01 09:05:00', '2025-08-01 18:10:00', 5, 0, 10, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-02', '2025-08-02 09:00:00', '2025-08-02 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 深夜勤務のテストケース（22:00以降）
(1, '2025-08-03', '2025-08-03 09:00:00', '2025-08-03 23:00:00', 0, 0, 300, 60, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-05', '2025-08-05 09:15:00', '2025-08-05 18:00:00', 15, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2025-08-06', '2025-08-06 09:00:00', '2025-08-06 18:00:00', 0, 0, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 跨ぎ日深夜勤務のテストケース
(1, '2025-08-07', '2025-08-07 21:00:00', '2025-08-08 02:00:00', 0, 0, 120, 240, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 早退のテストケース
(1, '2025-08-08', '2025-08-08 09:10:00', '2025-08-08 17:50:00', 10, 10, 0, 0, 'NORMAL', '未提出', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 有給取得日
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

-- テスト用有給申請データ（設計書通りのステータス）
INSERT INTO leave_requests (
    employee_id, leave_request_date, leave_request_reason, leave_request_status, 
    created_at, updated_at
) VALUES
(1, '2025-09-15', '私用のため', '未処理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2025-09-20', '家族の用事', '未処理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- テスト用打刻修正申請データ（設計書通りのステータス）
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

-- インデックス作成（設計書通りのパフォーマンス向上）
-- CREATE INDEX IF NOT EXISTS idx_employee_code ON employees(employee_code);
-- CREATE INDEX IF NOT EXISTS idx_employee_email ON employees(email);
-- CREATE INDEX IF NOT EXISTS idx_employee_status ON employees(employment_status);

-- CREATE INDEX IF NOT EXISTS idx_attendance_employee_date ON attendance_records(employee_id, attendance_date);
-- CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance_records(attendance_date);
-- CREATE INDEX IF NOT EXISTS idx_attendance_status ON attendance_records(attendance_status);
-- CREATE INDEX IF NOT EXISTS idx_submission_status ON attendance_records(submission_status);

-- CREATE INDEX IF NOT EXISTS idx_leave_employee_date ON leave_requests(employee_id, leave_request_date);
-- CREATE INDEX IF NOT EXISTS idx_leave_date ON leave_requests(leave_request_date);
-- CREATE INDEX IF NOT EXISTS idx_leave_status ON leave_requests(leave_request_status);

-- CREATE INDEX IF NOT EXISTS idx_adjustment_employee_date ON adjustment_requests(employee_id, adjustment_target_date);
-- CREATE INDEX IF NOT EXISTS idx_adjustment_date ON adjustment_requests(adjustment_target_date);
-- CREATE INDEX IF NOT EXISTS idx_adjustment_status ON adjustment_requests(adjustment_status);

-- 月末申請テスト用のサンプルデータ（一部申請済み状態）
UPDATE attendance_records 
SET submission_status = '申請済' 
WHERE employee_id = 2 
  AND attendance_date BETWEEN '2025-08-01' AND '2025-08-31'
  AND submission_status = '未提出';

-- 承認済みの勤怠データ（確定済み）のサンプル
UPDATE attendance_records 
SET submission_status = '承認', attendance_fixed_flag = 1
WHERE employee_id = 1 
  AND attendance_date = '2025-08-01';

-- 有給承認済みサンプル（残日数減算済み）
UPDATE leave_requests 
SET leave_request_status = '承認', approved_at = CURRENT_TIMESTAMP, approved_by_employee_id = 1
WHERE employee_id = 1 AND leave_request_date = '2025-08-09';

UPDATE employees 
SET paid_leave_remaining_days = paid_leave_remaining_days - 1
WHERE employee_id = 1;