CREATE TABLE patients (
    id UUID PRIMARY KEY,

    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    uh_id VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    email VARCHAR(150),

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE appointment_status AS ENUM (
    'CONFIRMED',
    'CANCELLED',
    'ADMITTED',
    'COMPLETED'
);

CREATE TABLE appointments (
    id UUID PRIMARY KEY,

    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    doctor_time_slot_id UUID NOT NULL,
    created_by_user_id UUID NOT NULL,

    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,

    doctor_designation_snapshot VARCHAR(150) NOT NULL,
    doctor_specialization_snapshot VARCHAR(150) NOT NULL,
    department_name_snapshot VARCHAR(255) NOT NULL,

    patient_message TEXT,
    appointment_status appointment_status NOT NULL,
    is_vip BOOLEAN NOT NULL DEFAULT FALSE,
    token_number INTEGER NOT NULL,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id),

    CONSTRAINT fk_appointments_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors (id),

    CONSTRAINT fk_appointments_doctor_time_slot
        FOREIGN KEY (doctor_time_slot_id) REFERENCES doctor_time_slots (id),

    CONSTRAINT fk_appointments_created_by_user
        FOREIGN KEY (created_by_user_id) REFERENCES users (id),

    CONSTRAINT uk_appointments_doctor_slot_date_token
        UNIQUE (doctor_id, doctor_time_slot_id, appointment_date, token_number)
);

CREATE INDEX idx_appointments_is_deleted
    ON appointments (is_deleted);

CREATE INDEX idx_appointments_appointment_date
    ON appointments (appointment_date);

CREATE INDEX idx_appointments_appointment_status
    ON appointments (appointment_status);
