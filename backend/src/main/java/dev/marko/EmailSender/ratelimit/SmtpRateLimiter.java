package dev.marko.EmailSender.ratelimit;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpRateLimiter {

    private final StringRedisTemplate redis;
    private final RateLimitConfig config;

    private DefaultRedisScript<Long> smtpLuaScript;

    private static final String SMTP_KEY_TEMPLATE = "rate_limit:smtp:%d";

    @PostConstruct
    void init() {
        smtpLuaScript = new DefaultRedisScript<>();
        smtpLuaScript.setLocation(new ClassPathResource("lua/smtp_rate_limit.lua"));
        smtpLuaScript.setResultType(Long.class);
    }

    /**
     * Check if email can be sent from this SMTP account
     */
    public boolean canSendEmail(SmtpCredentials smtp) {
        RateLimitConfig.SmtpRateLimit limits = getSmtpLimits(smtp.getSmtpType());

        List<String> keys = List.of(
                SMTP_KEY_TEMPLATE.formatted(smtp.getId()) + ":minute",
                SMTP_KEY_TEMPLATE.formatted(smtp.getId()) + ":hour",
                SMTP_KEY_TEMPLATE.formatted(smtp.getId()) + ":day"
        );

        try {
            Long result = redis.execute(
                    smtpLuaScript,
                    keys,
                    System.currentTimeMillis(),
                    1, // count = 1 email
                    Duration.ofMinutes(1).toMillis(),
                    Duration.ofHours(1).toMillis(),
                    Duration.ofDays(1).toMillis(),
                    limits.getEmailsPerMinute(),
                    limits.getEmailsPerHour(),
                    limits.getEmailsPerDay()
            );

            boolean allowed = result != null && result == 1;
            if (!allowed) {
                log.warn("SMTP {} hit rate limit, requeue email", smtp.getEmail());
            }
            return allowed;

        } catch (Exception e) {
            log.error("SMTP rate limiter failed-open for {}", smtp.getEmail(), e);
            return true; // fail-open
        }
    }

    public long getMinimumDelay(SmtpCredentials smtp) {
        return getSmtpLimits(smtp.getSmtpType()).getMinDelaySeconds();
    }

    private RateLimitConfig.SmtpRateLimit getSmtpLimits(SmtpType type) {
        String key = type.toString().toLowerCase();
        return config.getSmtp().getOrDefault(key,
                config.getSmtp().getOrDefault("default", new RateLimitConfig.SmtpRateLimit()));
    }
}