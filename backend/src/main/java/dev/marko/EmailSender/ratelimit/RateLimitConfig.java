package dev.marko.EmailSender.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    // SMTP Rate Limits (per account)
    private Map<String, SmtpRateLimit> smtp = new HashMap<>();

    // User Rate Limits
    private UserRateLimit user = new UserRateLimit();

    // Endpoint Rate Limits
    private Map<String, EndpointRateLimit> endpoint = new HashMap<>();

    @Data
    public static class SmtpRateLimit {
        private int emailsPerMinute = 60;
        private int emailsPerHour = 500;
        private int emailsPerDay = 2000;
        private int minDelaySeconds = 15; // minimum delay between emails
    }

    @Data
    public static class UserRateLimit {
        private int emailsPerHour = 100;
        private int emailsPerDay = 1000;
        private int batchRequestsPerHour = 10;
    }

    @Data
    public static class EndpointRateLimit {
        private int requestsPerMinute = 10;
        private int requestsPerHour = 100;
    }
}