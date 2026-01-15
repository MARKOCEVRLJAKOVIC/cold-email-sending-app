package dev.marko.EmailSender.redis;

import dev.marko.EmailSender.email.schedulers.EmailSendService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class EmailStreamWorker {

    private final StringRedisTemplate redis;
    private final EmailMessageRepository repo;
    private final EmailSendService emailSendService;
    private final Executor emailExecutor;

    public EmailStreamWorker(StringRedisTemplate redis,
                             EmailMessageRepository repo,
                             EmailSendService emailSendService,
                             @Qualifier("emailTaskExecutor") Executor emailExecutor) {
        this.redis = redis;
        this.repo = repo;
        this.emailSendService = emailSendService;
        this.emailExecutor = emailExecutor;
    }

    private static final String CONSUMER_GROUP = "email_send_group";
    private final String CONSUMER_NAME = "email-worker-" + UUID.randomUUID();
    private static final int MAX_RETRIES = 3;

    @PostConstruct
    public void init() {
        try {
            redis.opsForStream().createGroup(RedisKeys.SEND_STREAM, ReadOffset.from("0"), CONSUMER_GROUP);
            log.info("Consumer group {} initialized for worker {}", CONSUMER_GROUP, CONSUMER_NAME);
        } catch (Exception e) {
            log.info("Consumer group already exists.");
        }
    }

    @Scheduled(fixedRate = 1000)
    public void consume() {
        try {
            List<MapRecord<String, Object, Object>> messages = redis.opsForStream().read(
                    Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                    StreamReadOptions.empty().count(20),
                    StreamOffset.create(RedisKeys.SEND_STREAM, ReadOffset.lastConsumed())
            );

            if (messages == null || messages.isEmpty()) return;

            List<CompletableFuture<Void>> futures = messages.stream()
                    .map(msg -> CompletableFuture.runAsync(() -> processSingleMessage(msg), emailExecutor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } catch (DataAccessException e) {
            log.error("Redis error during consumption", e);
        }
    }

    private void processSingleMessage(MapRecord<String, Object, Object> msg) {
        String emailIdStr = (String) msg.getValue().get("emailId");
        if (emailIdStr == null) {
            acknowledge(msg.getId());
            return;
        }

        Long emailId = Long.valueOf(emailIdStr);
        int retryCount = Integer.parseInt(msg.getValue().getOrDefault("retry", "0").toString());

        try {
            EmailMessage email = repo.findById(emailId).orElse(null);

            if (email == null) {
                log.warn("Email {} not found, skipping.", emailId);
                acknowledge(msg.getId());
                return;
            }

            if (email.getStatus() != Status.PENDING) {
                log.info("Email {} status is {}, skipping send.", emailId, email.getStatus());
                acknowledge(msg.getId());
                return;
            }

            boolean success = emailSendService.sendAndPersist(email);

            if (success) {
                acknowledge(msg.getId());
            } else {
                handleFailure(emailId, retryCount, msg.getId());
            }

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Conflict detected for email {}. Someone else updated it. Skipping.", emailId);
            acknowledge(msg.getId());
        } catch (Exception e) {
            log.error("Fatal error processing email {}: {}", emailId, e.getMessage());
            handleFailure(emailId, retryCount, msg.getId());
        }
    }

    private void handleFailure(Long emailId, int retryCount, RecordId recordId) {
        if (retryCount >= MAX_RETRIES) {
            log.error("Email {} failed after {} retries. Moving to DLQ.", emailId, MAX_RETRIES);
            moveToDeadLetterQueue(emailId, retryCount);
        } else {
            requeueWithRetry(emailId, retryCount + 1);
        }
        acknowledge(recordId);
    }

    private void requeueWithRetry(Long emailId, int retry) {
        redis.opsForStream().add(StreamRecords.newRecord()
                .in(RedisKeys.SEND_STREAM)
                .ofMap(Map.of(
                        "emailId", emailId.toString(),
                        "retry", String.valueOf(retry)
                )));
    }

    private void moveToDeadLetterQueue(Long emailId, int retry) {
        redis.opsForStream().add(StreamRecords.newRecord()
                .in(RedisKeys.DEAD_LETTER_STREAM)
                .ofMap(Map.of(
                        "emailId", emailId.toString(),
                        "retry", String.valueOf(retry),
                        "failedAt", String.valueOf(System.currentTimeMillis())
                )));
    }

    private void acknowledge(RecordId recordId) {
        redis.opsForStream().acknowledge(RedisKeys.SEND_STREAM, CONSUMER_GROUP, recordId);
    }
}
