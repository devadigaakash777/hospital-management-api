ALTER TABLE health_package_appointments
ADD COLUMN created_by_user_id UUID;

ALTER TABLE health_package_appointments
ADD CONSTRAINT fk_health_package_appointments_created_by
FOREIGN KEY (created_by_user_id)
REFERENCES users(id);