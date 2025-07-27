ALTER TABLE smtp_credentials
ADD COLUMN oauth_access_token TEXT,
ADD COLUMN oauth_refresh_token TEXT,
ADD COLUMN token_expires_at BIGINT,
ADD COLUMN smtp_type VARCHAR(20),
MODIFY smtp_username VARCHAR(255) NULL,
MODIFY smtp_password VARCHAR(255) NULL;