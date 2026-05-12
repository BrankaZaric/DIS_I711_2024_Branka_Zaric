-- User Service Initial Data
-- Password: password123 (BCrypt hashed)
-- Admin Password: admin123 (BCrypt hashed)

INSERT INTO users (username, email, password, first_name, last_name, phone_number, role, active, created_at, updated_at)
VALUES
    ('admin', 'admin@ecommerce.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', '+1-555-0001', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('johndoe', 'john.doe@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe', '+1-555-0101', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('janedoe', 'jane.doe@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane', 'Doe', '+1-555-0102', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bobsmith', 'bob.smith@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Bob', 'Smith', '+1-555-0103', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('alicejohnson', 'alice.johnson@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Alice', 'Johnson', '+1-555-0104', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Note: All user passwords are 'password123' (hashed with BCrypt)
-- Admin password is 'admin123' (hashed with BCrypt)
