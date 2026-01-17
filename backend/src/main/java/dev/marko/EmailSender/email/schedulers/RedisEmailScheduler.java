package dev.marko.EmailSender.email.schedulers;

import dev.marko.EmailSender.redis.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.SchedulingException;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Objects;

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

//        try {
//            Boolean result = redis.opsForZSet().add(
//                    RedisKeys. SCHEDULED_EMAILS,
//                    emailId. toString(),
//                    deliveryTime
//            );
//
//            if (Boolean.TRUE.equals(result)) {
//                log.debug("Email [id={}] scheduled for epoch={} (delay={}s)",
//                        emailId, deliveryTime, delaySeconds);
//            } else {
//                log.warn("Email [id={}] was already scheduled, updating delivery time to epoch={}",
//                        emailId, deliveryTime);
//            }
//        } catch (Exception e) {
//            log.error("Failed to schedule email [id={}] in Redis:  {}", emailId, e.getMessage(), e);
//            throw new SchedulingException(
//                    String.format("Failed to schedule email [id=%d].  Redis service may be unavailable.", emailId),
//                    e
//            );
//        }
    }

    public void scheduleAt(Long emailId, LocalDateTime scheduledAt, long delaySeconds, String zone) {

        ZoneId zoneId = ZoneId.of(zone);
        ZoneOffset offset = scheduledAt.atZone(zoneId).getOffset();
        long baseEpoch = scheduledAt.toEpochSecond(offset);

        long deliveryTime = baseEpoch + delaySeconds;


        try {
            Boolean result = redis.opsForZSet().add(
                    RedisKeys.SCHEDULED_EMAILS,
                    emailId.toString(),
                    deliveryTime
            );
            if (Boolean.TRUE.equals(result)) {
                log.info("Scheduled email {} for specific time {} -> Delivery Epoch: {}", emailId, zone, deliveryTime);
            } else {
                log.warn("Email [id={}] was already scheduled, updating delivery time to epoch={}", emailId, deliveryTime);
            }
        } catch (Exception e) {
            throw new SchedulingException(String.format("Failed to schedule email [id=%d]", emailId), e);
        }
    }

    public void cancel(Long emailId) {
        Objects.requireNonNull(emailId, "Email ID cannot be null");

        try {
            Long removed = redis.opsForZSet().remove(RedisKeys.SCHEDULED_EMAILS, emailId. toString());

            if (removed != null && removed > 0) {
                log.info("Cancelled scheduled email [id={}]", emailId);
            } else {
                log.warn("Email [id={}] was not found in scheduled queue", emailId);
            }
        } catch (Exception e) {
            log.error("Failed to cancel scheduled email [id={}]: {}", emailId, e.getMessage(), e);
            throw new SchedulingException(
                    String.format("Failed to cancel scheduled email [id=%d]", emailId),
                    e
            );
        }
    }

    /**
     * Checks if an email is currently scheduled.
     *
     * @param emailId The email message ID to check
     * @return true if email is in the scheduling queue, false otherwise
     */
    public boolean isScheduled(Long emailId) {
        Objects.requireNonNull(emailId, "Email ID cannot be null");

        try {
            Double score = redis.opsForZSet().score(RedisKeys.SCHEDULED_EMAILS, emailId.toString());
            return score != null;
        } catch (Exception e) {
            log.error("Failed to check scheduling status for email [id={}]:  {}", emailId, e.getMessage(), e);
            throw new SchedulingException(
                    String.format("Failed to check scheduling status for email [id=%d]", emailId),
                    e
            );
        }
    }
}
