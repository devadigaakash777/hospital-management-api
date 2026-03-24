-- Enable UUID support
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ENUM for roles
CREATE TYPE user_role AS ENUM ('ADMIN', 'DOCTOR', 'STAFF');

-- =========================
-- DEPARTMENTS
-- =========================
CREATE TABLE departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    department_name VARCHAR(255) NOT NULL,

    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- =========================
-- USER GROUPS
-- =========================
CREATE TABLE user_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_name VARCHAR(255) NOT NULL,

    can_manage_doctor_slots BOOLEAN DEFAULT FALSE,
    can_manage_staff BOOLEAN DEFAULT FALSE,
    can_manage_groups BOOLEAN DEFAULT FALSE,
    can_export_reports BOOLEAN DEFAULT FALSE,
    can_manage_health_packages BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    role user_role NOT NULL,

    group_id UUID,
    CONSTRAINT fk_user_group FOREIGN KEY (group_id) REFERENCES user_groups(id),

    can_manage_doctor_slots BOOLEAN DEFAULT FALSE,
    can_manage_staff BOOLEAN DEFAULT FALSE,
    can_manage_groups BOOLEAN DEFAULT FALSE,
    can_export_reports BOOLEAN DEFAULT FALSE,
    can_manage_health_packages BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- =========================
-- GROUP - DEPARTMENTS (M2M)
-- =========================
CREATE TABLE group_departments (
    group_id UUID NOT NULL,
    department_id UUID NOT NULL,

    PRIMARY KEY (group_id, department_id),
    FOREIGN KEY (group_id) REFERENCES user_groups(id),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- =========================
-- USER - DEPARTMENTS (M2M)
-- =========================
CREATE TABLE user_departments (
    user_id UUID NOT NULL,
    department_id UUID NOT NULL,

    PRIMARY KEY (user_id, department_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- =========================
-- INDEXES
-- =========================
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_group_id ON users(group_id);