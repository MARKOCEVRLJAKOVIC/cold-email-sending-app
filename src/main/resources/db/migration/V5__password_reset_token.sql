CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT password_reset_token_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);