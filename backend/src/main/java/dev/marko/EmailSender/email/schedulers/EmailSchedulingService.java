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

/**
 * Service for scheduling email messages for future delivery.
 * Handles single email scheduling, batch scheduling, and follow-up scheduling with retry logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSchedulingService {

    private final RedisEmailScheduler redisEmailScheduler;

    @Getter
    @Value("${email.scheduling.default-delay-seconds}")
    private int defaultDelay;

    /**
     * Schedules a single email message for delivery at a specific time with timezone support.
     * Uses retry logic with exponential backoff in case of scheduling failures.
     *
     * @param message the email message to schedule
     * @param scheduledAt the target delivery time
     * @param intervalDelaySeconds additional delay in seconds to add to the scheduled time
     * @throws SchedulingException if scheduling fails after retries
     */
    @Retryable(
            value = {SchedulingException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void scheduleSingle(EmailMessage message, LocalDateTime scheduledAt, long intervalDelaySeconds) {

        String zone = message.getCampaign().getTimezone();
        redisEmailScheduler.scheduleAt(message.getId(), scheduledAt, intervalDelaySeconds, zone);

    }

    /**
     * Schedules a batch of email messages with intervals between each email.
     * Each email is scheduled with an incremental delay based on its position in the list.
     * Uses retry logic with exponential backoff in case of scheduling failures.
     *
     * @param messages the list of email messages to schedule
     * @param intervalInSeconds the delay in seconds between each email in the batch
     * @throws SchedulingException if scheduling fails after retries
     */
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

    /**
     * Schedules a follow-up email message with a specified delay.
     *
     * @param message the follow-up email message to schedule
     * @param delayInSeconds the delay in seconds before sending the email
     */
    public void scheduleFollowUp(EmailMessage message, long delayInSeconds) {

        redisEmailScheduler.schedule(message.getId(), delayInSeconds);

    }

    /**
     * Cancels a previously scheduled email message.
     *
     * @param emailId the ID of the email message to cancel
     * @throws SchedulingException if cancellation fails
     */
    public void cancelScheduled(Long emailId) {
        try {
            redisEmailScheduler.cancel(emailId);
            log.info("Successfully cancelled scheduled email [id={}]", emailId);
        } catch (SchedulingException e) {
            log.error("Failed to cancel scheduled email [id={}]:  {}", emailId, e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if an email message is currently scheduled for delivery.
     *
     * @param emailId the ID of the email message to check
     * @return true if the email is scheduled, false otherwise
     * @throws SchedulingException if the check fails
     */
    public boolean isScheduled(Long emailId) {
        try {
            return redisEmailScheduler.isScheduled(emailId);
        } catch (SchedulingException e) {
            log.error("Failed to check scheduling status for email [id={}]: {}", emailId, e.getMessage());
            throw e;
        }
    }

}