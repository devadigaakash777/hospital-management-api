ALTER TABLE email_verification_tokens
    ALTER COLUMN payload DROP NOT NULL,
    ADD COLUMN user_id       UUID         NULL,
    ADD COLUMN pending_email VARCHAR(150) NULL;

CREATE INDEX idx_evt_user_id ON email_verification_tokens(user_id);