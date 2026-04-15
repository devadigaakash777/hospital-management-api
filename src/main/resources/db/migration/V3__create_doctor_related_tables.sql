CREATE TYPE day_of_week AS ENUM (
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY'
);

CREATE TABLE doctors (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL UNIQUE,
    department_id UUID NOT NULL,

    qualification VARCHAR(255) NOT NULL,
    designation VARCHAR(150) NOT NULL,
    specialization VARCHAR(150) NOT NULL,
    room_number VARCHAR(20),

    advance_booking_days INTEGER NOT NULL,
    photo_url VARCHAR(500),

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_doctors_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_doctors_department
        FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE INDEX idx_doctors_department_id
    ON doctors(department_id);

CREATE TABLE doctor_weekly_schedules (
    id UUID PRIMARY KEY,

    doctor_id UUID NOT NULL,
    week_number INTEGER NOT NULL,
    day_of_week day_of_week NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_doctor_weekly_schedules_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id),

    CONSTRAINT uk_doctor_weekly_schedule_doctor_week_day
        UNIQUE (doctor_id, week_number, day_of_week)
);

CREATE INDEX idx_doctor_weekly_schedules_doctor_id
    ON doctor_weekly_schedules(doctor_id);

CREATE TABLE doctor_time_slots (
    id UUID PRIMARY KEY,

    doctor_id UUID NOT NULL,

    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    patients_per_slot INTEGER NOT NULL,
    reserved_slots INTEGER NOT NULL DEFAULT 0,
    total_slots INTEGER NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_doctor_time_slots_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE INDEX idx_doctor_time_slots_doctor_id
    ON doctor_time_slots(doctor_id);

CREATE TABLE doctor_blocked_dates (
    id UUID PRIMARY KEY,

    doctor_id UUID NOT NULL,

    blocked_date DATE NOT NULL,
    block_reason VARCHAR(1000),

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_doctor_blocked_dates_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id),

    CONSTRAINT uk_doctor_blocked_dates_doctor_date
        UNIQUE (doctor_id, blocked_date)
);

CREATE INDEX idx_doctor_blocked_dates_doctor_id
    ON doctor_blocked_dates(doctor_id);

CREATE TABLE doctor_blocked_time_slots (
    id UUID PRIMARY KEY,

    batch_id UUID,
    doctor_id UUID NOT NULL,

    blocked_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    reserved_slots INTEGER NOT NULL DEFAULT 0,
    block_reason VARCHAR(1000),

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_doctor_blocked_time_slots_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id),

    CONSTRAINT uk_doctor_blocked_time_slot_doctor_date_time
        UNIQUE (doctor_id, blocked_date, start_time, end_time)
);

CREATE INDEX idx_doctor_blocked_time_slots_doctor_id
    ON doctor_blocked_time_slots(doctor_id);