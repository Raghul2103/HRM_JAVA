-- V1__init_schema.sql
-- Database Initialization Schema for Construction Workforce HRMS

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

-- 2. Workers Table
CREATE TABLE IF NOT EXISTS workers (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    designation VARCHAR(50) NOT NULL,
    daily_wage_rate NUMERIC(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_workers_phone ON workers (phone);

-- 3. Sites Table
CREATE TABLE IF NOT EXISTS sites (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    site_name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. Attendance Logs Table
CREATE TABLE IF NOT EXISTS attendance_logs (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    worker_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    clock_in TIMESTAMP NOT NULL,
    clock_out TIMESTAMP,
    total_hours NUMERIC(5, 2),
    overtime_hours NUMERIC(5, 2),
    flagged BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_attendance_worker_clock_in ON attendance_logs (worker_id, clock_in);
CREATE INDEX IF NOT EXISTS idx_attendance_site_clock_in ON attendance_logs (site_id, clock_in);

-- 5. Overtime Entries Table
CREATE TABLE IF NOT EXISTS overtime_entries (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    worker_id BIGINT NOT NULL,
    attendance_log_id BIGINT NOT NULL,
    date DATE NOT NULL,
    overtime_hours NUMERIC(5, 2) NOT NULL,
    overtime_rate_applied NUMERIC(10, 2) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    settlement_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    month_string VARCHAR(7) NOT NULL,
    FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE CASCADE,
    FOREIGN KEY (attendance_log_id) REFERENCES attendance_logs(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_overtime_worker_month ON overtime_entries (worker_id, month_string);
