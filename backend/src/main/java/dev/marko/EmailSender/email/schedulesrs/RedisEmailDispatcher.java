package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.redis.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisEmailDispatcher {

    private final StringRedisTemplate redis;

    @Scheduled(fixedRate = 5000)
    public void dispatchReadyEmails() {
        long now = Instant.now().getEpochSecond();

        // Find ready emails
        Set<String> ready = redis.opsForZSet().rangeByScore(
                RedisKeys.SCHEDULED_EMAILS, 0, now);

        if (ready == null || ready.isEmpty()) return;

        for (String emailId : ready) {
            // Remove from ZSET
            redis.opsForZSet().remove(RedisKeys.SCHEDULED_EMAILS, emailId);

            // Body for stream
            Map<String, String> body = Map.of("emailId", emailId);

            RecordId recordId = redis.opsForStream().add(
                    StreamRecords.newRecord()
                            .in(RedisKeys.SEND_STREAM)
                            .ofMap(body)
            );

            log.info("Moved email {} to stream (record {})", emailId, recordId);
        }
    }
}