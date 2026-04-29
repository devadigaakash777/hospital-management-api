CREATE TABLE notifications (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID          NOT NULL,
    title      VARCHAR(200)  NOT NULL,
    message    TEXT          NOT NULL,
    data       JSONB,
    is_read    BOOLEAN       DEFAULT FALSE,
    created_at TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);