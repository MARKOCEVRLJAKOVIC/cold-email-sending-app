package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.redis.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisEmailScheduler {

    private final StringRedisTemplate redis;

    /**
     * Schedules email by adding to ZSET with timestamp score
     */
    public void schedule(Long emailId, long delaySeconds) {
        long deliveryTime = Instant.now().getEpochSecond() + delaySeconds;

        redis.opsForZSet().add(RedisKeys.SCHEDULED_EMAILS,
                emailId.toString(),
                deliveryTime);

        log.info("Scheduled email {} at {}", emailId, deliveryTime);
    }
}

