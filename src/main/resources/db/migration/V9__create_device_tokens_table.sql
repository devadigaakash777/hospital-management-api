CREATE TABLE user_device_tokens (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL,
    fcm_token     TEXT         NOT NULL UNIQUE,
    device_type   VARCHAR(20),  -- 'ANDROID' | 'IOS'
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_device_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_device_tokens_user_id ON user_device_tokens(user_id);