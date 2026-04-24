CREATE TABLE health_packages (
    id UUID PRIMARY KEY,

    package_name VARCHAR(150) NOT NULL,
    description TEXT,
    package_price NUMERIC(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    advance_booking_days INTEGER NOT NULL,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE health_package_weekly_schedules (
    id UUID PRIMARY KEY,

    health_package_id UUID NOT NULL,
    week_number INTEGER NOT NULL,
    day_of_week day_of_week NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_health_package_weekly_schedules_health_package
        FOREIGN KEY (health_package_id) REFERENCES health_packages (id),

    CONSTRAINT uk_health_package_weekly_schedule_package_week_day
        UNIQUE (health_package_id, week_number, day_of_week)
);

CREATE INDEX idx_health_package_weekly_schedules_health_package_id
    ON health_package_weekly_schedules (health_package_id);

CREATE TABLE health_package_time_slots (
    id UUID PRIMARY KEY,

    health_package_id UUID NOT NULL,

    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    total_slots INTEGER NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_health_package_time_slots_health_package
        FOREIGN KEY (health_package_id) REFERENCES health_packages (id),

    CONSTRAINT uk_health_package_time_slot_package_start_end
        UNIQUE (health_package_id, start_time, end_time)
);

CREATE INDEX idx_health_package_time_slots_health_package_id
    ON health_package_time_slots (health_package_id);

CREATE TABLE health_package_appointments (
    id UUID PRIMARY KEY,

    health_package_id UUID NOT NULL,
    health_package_time_slot_id UUID NOT NULL,
    patient_id UUID NOT NULL,

    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    appointment_status appointment_status NOT NULL,
    token_number INTEGER NOT NULL,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_health_package_appointments_health_package
        FOREIGN KEY (health_package_id) REFERENCES health_packages (id),

    CONSTRAINT fk_health_package_appointments_time_slot
        FOREIGN KEY (health_package_time_slot_id) REFERENCES health_package_time_slots (id),

    CONSTRAINT fk_health_package_appointments_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id),

    CONSTRAINT uk_health_package_appointments_package_slot_date_token
        UNIQUE (health_package_id, health_package_time_slot_id, appointment_date, token_number)
);

CREATE INDEX idx_health_package_appointments_health_package_id
    ON health_package_appointments (health_package_id);

CREATE INDEX idx_health_package_appointments_patient_id
    ON health_package_appointments (patient_id);

CREATE INDEX idx_health_package_appointments_appointment_date
    ON health_package_appointments (appointment_date);

CREATE INDEX idx_health_package_appointments_appointment_status
    ON health_package_appointments (appointment_status);

CREATE INDEX idx_health_package_appointments_is_deleted
    ON health_package_appointments (is_deleted);