ALTER TABLE email_messages
ADD COLUMN follow_up_template_id BIGINT NULL,
ADD CONSTRAINT fk_follow_up_template
    FOREIGN KEY (follow_up_template_id) REFERENCES follow_up_templates(id) ON DELETE SET NULL;
