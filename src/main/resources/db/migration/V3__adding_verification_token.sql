CREATE TABLE verification_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    CONSTRAINT verification_token_users_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE users ADD COLUMN enabled BOOLEAN DEFAULT FALSE NOT NULL;