
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER' NOT NULL
);

CREATE TABLE smtp_credentials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    sender_email VARCHAR(255) NOT NULL,
    smtp_host VARCHAR(255) NOT NULL,
    smtp_port INT NOT NULL,
    smtp_username VARCHAR(255) NOT NULL,
    smtp_password VARCHAR(255) NOT NULL,

    user_id BIGINT NOT NULL,
    CONSTRAINT smtp_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE campaigns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) DEFAULT 'Untitled Campaign',
    description TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT campaigns_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE email_templates (
    id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,

    campaign_id BIGINT,
    user_id BIGINT NOT NULL,

    CONSTRAINT email_templates_campaigns_fk FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE SET NULL,
    CONSTRAINT email_templates_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE email_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    sent_message TEXT NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',

    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    smtp_id BIGINT,
    campaign_id BIGINT,

    CONSTRAINT email_messages_users_fk FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT email_messages_templates_fk FOREIGN KEY (template_id) REFERENCES email_templates(id),
    CONSTRAINT email_messages_smtp_credentials_fk FOREIGN KEY (smtp_id) REFERENCES smtp_credentials(id),
    CONSTRAINT email_messages_campaigns_fk FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE SET NULL
);