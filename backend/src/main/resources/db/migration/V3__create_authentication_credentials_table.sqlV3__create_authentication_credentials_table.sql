CREATE TABLE authentication_credentials (
    id BIGSERIAL PRIMARY KEY,
    authentication_enrollment_id BIGINT NOT NULL UNIQUE,
    credential_value VARCHAR(1000) NOT NULL,
    metadata VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_authentication_credentials_enrollment
        FOREIGN KEY (authentication_enrollment_id)
        REFERENCES authentication_enrollments(id)
        ON DELETE CASCADE
);