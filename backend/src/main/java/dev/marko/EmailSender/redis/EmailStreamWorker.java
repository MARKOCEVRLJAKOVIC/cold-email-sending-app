package dev.marko.EmailSender.redis;

import dev.marko.EmailSender.email.schedulesrs.EmailSendService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailStreamWorker {

    private final StringRedisTemplate redis;
    private final EmailMessageRepository repo;
    private final EmailSendService emailSendService;

    private static final String CONSUMER = "email-worker-1";

    @PostConstruct
    public void init() {
        try {
            redis.opsForStream().createGroup(
                    RedisKeys.SEND_STREAM,
                    ReadOffset.from("0"),
                    RedisKeys.SEND_GROUP
            );
            log.info("Created consumer group {}", RedisKeys.SEND_GROUP);

        } catch (Exception e) {
            log.info("Consumer group already exists, continuing...");
        }
    }

    @Scheduled(fixedRate = 2000)
    public void consume() {
        try {
            List<MapRecord<String, Object, Object>> messages =
                    redis.opsForStream().read(
                            Consumer.from(RedisKeys.SEND_GROUP, CONSUMER),
                            StreamReadOptions.empty().count(10),
                            StreamOffset.create(RedisKeys.SEND_STREAM, ReadOffset.lastConsumed())
                    );

            if (messages == null || messages.isEmpty()) return;

            for (MapRecord<String, Object, Object> msg : messages) {
                try {
                    Map<Object, Object> value = msg.getValue();
                    String emailIdStr = (String) value.get("emailId");
                    Long emailId = Long.valueOf(emailIdStr);

                    EmailMessage email = repo.findById(emailId).orElse(null);
                    if (email == null) continue;

                    emailSendService.sendAndPersist(email);

                    redis.opsForStream().acknowledge(
                            RedisKeys.SEND_STREAM,
                            RedisKeys.SEND_GROUP,
                            msg.getId()
                    );

                } catch (Exception e) {
                    log.error("Error processing message {}", msg.getId(), e);
                }
            }
        } catch (DataAccessException e) {
            log.error("Redis stream read error", e);
        }
    }
}