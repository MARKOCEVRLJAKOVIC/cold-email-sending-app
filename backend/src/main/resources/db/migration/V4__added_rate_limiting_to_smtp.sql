ALTER TABLE smtp_credentials
    ADD COLUMN emails_per_second DOUBLE NOT NULL DEFAULT 0.1;