ALTER TABLE email_messages
ADD CONSTRAINT unique_followup_per_original
UNIQUE (in_reply_to, follow_up_template_id);