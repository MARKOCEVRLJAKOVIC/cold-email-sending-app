
package dev.marko.EmailSender.ratelimit;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final StringRedisTemplate redis;

    private DefaultRedisScript<Long> userEmailScript;

    private static final String USER_HOUR_KEY = "rate_limit:user:%d:hour";
    private static final String USER_DAY_KEY  = "rate_limit:user:%d:day";

    @PostConstruct
    void init() {
        userEmailScript = new DefaultRedisScript<>();
        userEmailScript.setLocation(
                new ClassPathResource("lua/user_email_rate_limit.lua")
        );
        userEmailScript.setResultType(Long.class);
    }

    public boolean tryConsumeUserEmails(
            Long userId,
            int count,
            RateLimitConfig.UserRateLimit config
    ) {

        long now = Instant.now().toEpochMilli();

        List<String> keys = List.of(
                USER_HOUR_KEY.formatted(userId),
                USER_DAY_KEY.formatted(userId)
        );

        try {
            Long result = redis.execute(
                    userEmailScript,
                    keys,
                    now,
                    Duration.ofHours(1).toMillis(),
                    Duration.ofDays(1).toMillis(),
                    config.getEmailsPerHour(),
                    config.getEmailsPerDay(),
                    count
            );

            return result != null && result == 1;

        } catch (Exception e) {
            log.error("User rate limit failed-open for user {}", userId, e);
            return true; // fail-open (mail system - strict safety)
        }
    }
}
