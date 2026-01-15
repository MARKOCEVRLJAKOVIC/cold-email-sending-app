package dev.marko.EmailSender.email.schedulers;

import dev.marko.EmailSender.redis.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.SchedulingException;
import org.springframework.stereotype.Service;

import java.time.*;

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

        //long deliveryTime = Instant.now().getEpochSecond() + delaySeconds;
        try {
            Boolean result = redis.opsForZSet().add(
                    RedisKeys. SCHEDULED_EMAILS,
                    emailId. toString(),
                    deliveryTime
            );

            if (Boolean.TRUE.equals(result)) {
                log.debug("Email [id={}] scheduled for epoch={} (delay={}s)",
                        emailId, deliveryTime, delaySeconds);
            } else {
                log.warn("Email [id={}] was already scheduled, updating delivery time to epoch={}",
                        emailId, deliveryTime);
            }
        } catch (Exception e) {
            log.error("Failed to schedule email [id={}] in Redis:  {}", emailId, e.getMessage(), e);
            throw new SchedulingException(
                    String.format("Failed to schedule email [id=%d].  Redis service may be unavailable.", emailId),
                    e
            );
        }
    }

    public void scheduleAt(Long emailId, LocalDateTime scheduledAt, long delaySeconds, String zone) {

        ZoneId zoneId = ZoneId.of(zone);
        ZoneOffset offset = scheduledAt.atZone(zoneId).getOffset();
        long baseEpoch = scheduledAt.toEpochSecond(offset);

        long deliveryTime = baseEpoch + delaySeconds;


        redis.opsForZSet().add(
                RedisKeys.SCHEDULED_EMAILS,
                emailId.toString(),
                deliveryTime
        );

        log.info("Scheduled email {} for specific time {} -> Delivery Epoch: {}",
                emailId, zone, deliveryTime);

    }
}

