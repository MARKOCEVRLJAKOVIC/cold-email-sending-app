ALTER TABLE email_messages
ADD COLUMN error_message LONGTEXT NULL AFTER in_reply_to;