-- Chèn dữ liệu mẫu cho bảng Permissions
INSERT INTO permissions (name) VALUES ('CONTROL_SYSTEM') ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name) VALUES ('EXPORT_DATA') ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name) VALUES ('VIEW_HISTORY') ON CONFLICT (name) DO NOTHING;

-- Chèn dữ liệu mẫu cho bảng Roles
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('OPERATOR') ON CONFLICT (name) DO NOTHING;

-- Chèn dữ liệu phân quyền (Role_Permissions)
-- Giả định Role ADMIN có toàn quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name IN ('CONTROL_SYSTEM', 'EXPORT_DATA', 'VIEW_HISTORY')
ON CONFLICT DO NOTHING;

-- Giả định Role OPERATOR có quyền CONTROL_SYSTEM và VIEW_HISTORY
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'OPERATOR' AND p.name IN ('CONTROL_SYSTEM', 'VIEW_HISTORY')
ON CONFLICT DO NOTHING;

-- Tùy chọn: Chèn 1 tài khoản Admin mặc định (Password là chuỗi đã mã hóa hoặc plaintext tùy cấu hình hệ thống, ở đây là '123456' để minh họa)
-- Bạn có thể cập nhật password theo mã hóa BCrypt nếu đã cấu hình Spring Security.
INSERT INTO users (username, password, enabled)
VALUES ('admin', '{noop}123456', true)
ON CONFLICT (username) DO NOTHING;

-- Gán quyền ADMIN cho user 'admin'
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
