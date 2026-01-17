package dev.marko.EmailSender.email.schedulers;

import dev.marko.EmailSender.entities.EmailMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.SchedulingException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSchedulingService {

    private final RedisEmailScheduler redisEmailScheduler;

    @Getter
    @Value("${email.scheduling.default-delay-seconds}")
    private int defaultDelay;


    @Retryable(
            value = {SchedulingException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void scheduleSingle(EmailMessage message, LocalDateTime scheduledAt, long intervalDelaySeconds) {

        String zone = message.getCampaign().getTimezone();
        redisEmailScheduler.scheduleAt(message.getId(), scheduledAt, intervalDelaySeconds, zone);

    }

    @Retryable(
            value = {SchedulingException. class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    public void scheduleBatch(List<EmailMessage> messages, long intervalInSeconds) {
        if (messages == null || messages.isEmpty()) {
            log.warn("Attempted to schedule empty batch");
            return;
        }

        for (int i = 0; i < messages.size(); i++) {
            EmailMessage message = messages.get(i);
            long delay = i * intervalInSeconds;

            redisEmailScheduler.schedule(message.getId(), delay);

        }

    }

    public void scheduleFollowUp(EmailMessage message, long delayInSeconds) {

        redisEmailScheduler.schedule(message.getId(), delayInSeconds);

    }

    public void cancelScheduled(Long emailId) {
        try {
            redisEmailScheduler.cancel(emailId);
            log.info("Successfully cancelled scheduled email [id={}]", emailId);
        } catch (SchedulingException e) {
            log.error("Failed to cancel scheduled email [id={}]:  {}", emailId, e.getMessage());
            throw e;
        }
    }

    public boolean isScheduled(Long emailId) {
        try {
            return redisEmailScheduler.isScheduled(emailId);
        } catch (SchedulingException e) {
            log.error("Failed to check scheduling status for email [id={}]: {}", emailId, e.getMessage());
            throw e;
        }
    }

}