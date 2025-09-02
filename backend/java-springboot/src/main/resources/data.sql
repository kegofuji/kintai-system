-- 初期データ
INSERT INTO employees (employee_code, employee_name, email, employee_password_hash, employee_role, employment_status, hired_at, paid_leave_remaining_days, created_at, updated_at) VALUES
('ADMIN001', '管理者', 'admin@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin', 'active', '2025-01-01', 10, NOW(), NOW()),
('E001', '山田太郎', 'yamada@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'employee', 'active', '2025-01-01', 10, NOW(), NOW()),
('E002', '鈴木花子', 'suzuki@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'employee', 'active', '2025-02-01', 10, NOW(), NOW()),
('E003', '佐藤次郎', 'sato@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'employee', 'retired', '2024-04-01', 5, NOW(), NOW());