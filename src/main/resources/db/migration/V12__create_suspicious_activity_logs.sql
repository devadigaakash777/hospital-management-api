-- ENUM for suspicious event types
CREATE TYPE suspicious_event_type AS ENUM (
    'REVOKED_TOKEN_REUSE',
    'EXPIRED_TOKEN_REUSE',
    'INVALID_TOKEN_ATTEMPT',
    'LOGIN_FAILURE',
    'BRUTE_FORCE_DETECTED'
);

-- =========================
-- SUSPICIOUS ACTIVITY LOGS
-- =========================
CREATE TABLE suspicious_activity_logs (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_type suspicious_event_type NOT NULL,
    email      VARCHAR(150),
    ip_address VARCHAR(50),
    user_agent VARCHAR(512),
    token_hint VARCHAR(8),
    detail     VARCHAR(500),

    created_at TIMESTAMP NOT NULL
);

-- =========================
-- INDEXES
-- =========================
CREATE INDEX idx_sus_email      ON suspicious_activity_logs (email);
CREATE INDEX idx_sus_ip         ON suspicious_activity_logs (ip_address);
CREATE INDEX idx_sus_event_type ON suspicious_activity_logs (event_type);
CREATE INDEX idx_sus_created_at ON suspicious_activity_logs (created_at);