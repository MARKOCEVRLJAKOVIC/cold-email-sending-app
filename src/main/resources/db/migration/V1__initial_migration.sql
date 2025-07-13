

CREATE TABLE users
(
    id BIGINT   PRIMARY KEY AUTO_INCREMENT NOT NULL,
    username    VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL

);

CREATE TABLE smtp_credentials (
    id BIGINT       PRIMARY KEY AUTO_INCREMENT NOT NULL,
    sender_email    VARCHAR(255) NOT NULL,
    smtp_host       VARCHAR(255) NOT NULL,
    smtp_port       INT NOT NULL,
    smtp_username   VARCHAR(255) NOT NULL,
    smtp_password   VARCHAR(255) NOT NULL,
    user_id BIGINT  NOT NULL,

    CONSTRAINT      smtp_users_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE email_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    recipient_name VARCHAR(255),
    recipient_email VARCHAR(255),
    subject VARCHAR(255),
    message TEXT,
    chosen_template TEXT,
    status  VARCHAR(20) DEFAULT 'PENDING',
    user_id BIGINT NOT NULL,
    CONSTRAINT email_send_users_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

