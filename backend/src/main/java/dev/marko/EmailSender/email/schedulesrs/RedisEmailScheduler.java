package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.redis.RedisKeys;
import dev.marko.EmailSender.repositories.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
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

        redis.opsForZSet().add(RedisKeys.SCHEDULED_EMAILS,
                emailId.toString(),
                deliveryTime);

        log.info("Scheduled email {} at {}", emailId, deliveryTime);
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

