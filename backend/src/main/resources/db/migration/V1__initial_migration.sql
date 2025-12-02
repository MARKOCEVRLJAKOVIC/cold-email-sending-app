
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER' NOT NULL,
    enabled BOOLEAN DEFAULT FALSE NOT NULL,
    UNIQUE KEY idx_users_email (email),
    UNIQUE KEY idx_users_username (username)
);

CREATE TABLE smtp_credentials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    sender_email VARCHAR(255) NOT NULL,
    smtp_host VARCHAR(255) NOT NULL,
    smtp_port INT NOT NULL,
    smtp_username VARCHAR(255) NULL,
    smtp_password VARCHAR(255) NULL,
    oauth_access_token TEXT,
    oauth_refresh_token TEXT,
    token_expires_at BIGINT,
    smtp_type VARCHAR(20),
    last_checked_uid BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    CONSTRAINT smtp_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_smtp_user (user_id),
    INDEX idx_smtp_email (sender_email)
);

CREATE TABLE campaigns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) DEFAULT 'Untitled Campaign',
    description TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    timezone VARCHAR(64) DEFAULT 'Europe/Belgrade',
    CONSTRAINT campaigns_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_campaign_user (user_id)
);

CREATE TABLE email_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    campaign_id BIGINT,
    user_id BIGINT NOT NULL,
    CONSTRAINT email_templates_campaigns_fk FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE SET NULL,
    CONSTRAINT email_templates_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_template_user (user_id),
    INDEX idx_template_campaign (campaign_id)
);

CREATE TABLE follow_up_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    delay_days INT NOT NULL,
    message TEXT NOT NULL,
    template_order INT DEFAULT 1,
    campaign_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT followup_campaign_fk FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    CONSTRAINT followup_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_followup_campaign (campaign_id),
    INDEX idx_followup_user (user_id)
);

CREATE TABLE email_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    scheduled_at TIMESTAMP NULL,
    sent_message TEXT NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED', 'REPLIED') DEFAULT 'PENDING',
    message_id VARCHAR(255),
    in_reply_to VARCHAR(255),
    error_message LONGTEXT NULL,
    user_id BIGINT NOT NULL,
    template_id BIGINT NULL,
    smtp_id BIGINT,
    campaign_id BIGINT,
    follow_up_template_id BIGINT NULL,
    CONSTRAINT email_messages_users_fk FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT email_messages_templates_fk FOREIGN KEY (template_id) REFERENCES email_templates(id),
    CONSTRAINT email_messages_smtp_credentials_fk FOREIGN KEY (smtp_id) REFERENCES smtp_credentials(id),
    CONSTRAINT email_messages_campaigns_fk FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE SET NULL,
    CONSTRAINT fk_follow_up_template FOREIGN KEY (follow_up_template_id) REFERENCES follow_up_templates(id) ON DELETE SET NULL,
    INDEX idx_email_user (user_id),
    INDEX idx_email_campaign (campaign_id),
    INDEX idx_email_status (status),
    INDEX idx_email_scheduled (scheduled_at),
    INDEX idx_email_template (template_id)
);

CREATE TABLE email_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_message_id VARCHAR(255),
    replied_message_id VARCHAR(255),
    sender_email VARCHAR(255),
    received_at TIMESTAMP,
    subject TEXT,
    content TEXT,
    email_message_id BIGINT,
    user_id BIGINT NULL,
    CONSTRAINT email_replies_messages_fk FOREIGN KEY (email_message_id) REFERENCES email_messages(id),
    CONSTRAINT email_replies_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reply_message (email_message_id),
    INDEX idx_reply_user (user_id)
);

CREATE TABLE verification_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL UNIQUE,
    expiration_date TIMESTAMP NOT NULL,
    CONSTRAINT verification_token_users_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT password_reset_token_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_password_token_user (user_id)
);
