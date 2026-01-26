package dev.marko.EmailSender.ratelimit;

import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.RateLimitExceededException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRateLimiter {

    private final RateLimiterService rateLimiterService;
    private final RateLimitConfig config;
    private final StringRedisTemplate redis;

    private DefaultRedisScript<Long> batchLimitScript;

    @PostConstruct
    public void init() {
        batchLimitScript = new DefaultRedisScript<>();
        batchLimitScript.setLocation(new ClassPathResource("lua/user_batch_rate_limit.lua"));
        batchLimitScript.setResultType(Long.class);
    }

    public void consumeEmailsOrThrow(User user, int count) {
        boolean allowed = rateLimiterService.tryConsumeUserEmails(
                user.getId(),
                count,
                config.getUser()
        );

        if (!allowed) {
            log.warn("User {} exceeded email quota for {} emails", user.getId(), count);
            throw new RateLimitExceededException("User email quota exceeded");
        }
    }

    public void consumeBatchOrThrow(User user) {
        String batchKey = "rate_limit:user:" + user.getId() + ":batch";

        String script =
                "local current = redis.call('INCR', KEYS[1]); " +
                        "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end; " +
                        "return current;";

        Long current = redis.execute(new DefaultRedisScript<>(script, Long.class),
                List.of(batchKey),
                String.valueOf(Duration.ofHours(1).toSeconds()));

        if (current != null && current > config.getUser().getBatchRequestsPerHour()) {
            log.warn("User {} exceeded batch request limit", user.getId());
            throw new RateLimitExceededException("User batch request quota exceeded");
        }
    }
}