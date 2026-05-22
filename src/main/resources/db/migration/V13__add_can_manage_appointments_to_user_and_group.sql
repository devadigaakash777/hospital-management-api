ALTER TABLE users
    ADD COLUMN can_manage_appointments BOOLEAN DEFAULT FALSE;

ALTER TABLE user_groups
    ADD COLUMN can_manage_appointments BOOLEAN DEFAULT FALSE;