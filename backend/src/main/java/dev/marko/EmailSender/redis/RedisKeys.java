package dev.marko.EmailSender.redis;

public class RedisKeys {
    public static final String SCHEDULED_EMAILS = "scheduled_emails"; // ZSET
    public static final String SEND_STREAM = "email_send_queue"; // STREAM
    public static final String SEND_GROUP = "email_send_group"; // CONSUMER GROUP
}
