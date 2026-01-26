package dev.marko.EmailSender.email.send.batch;

import dev.marko.EmailSender.email.schedulers.EmailSchedulingService;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

/**
 * Service for scheduling batches of email messages.
 * Handles both immediate scheduling with intervals and scheduled time-based scheduling with timezone support.
 */
@AllArgsConstructor
@Service
public class BatchSchedulingService {

    private final EmailSchedulingService emailSchedulingService;

    /**
     * Schedules a batch of email messages for delivery.
     * If scheduledAt is null, emails are scheduled immediately with intervals.
     * If scheduledAt is provided, emails are scheduled for that time with incremental delays based on campaign timezone.
     *
     * @param scheduledAt the target scheduled time, or null for immediate scheduling
     * @param allMessages the list of email messages to schedule
     * @param campaign the campaign containing timezone information
     */
    public void scheduleEmails(LocalDateTime scheduledAt, List<EmailMessage> allMessages, Campaign campaign) {
        if (allMessages.isEmpty()) return;

        long defaultDelay = emailSchedulingService.getDefaultDelay();

        if (scheduledAt == null) {
            emailSchedulingService.scheduleBatch(allMessages, defaultDelay);
            return;
        }

        ZoneId zone = ZoneId.of(campaign.getTimezone());

        for (int i = 0; i < allMessages.size(); i++) {
            long intervalDelay = i * defaultDelay;
            emailSchedulingService.scheduleSingle(allMessages.get(i), scheduledAt, intervalDelay);
        }
    }
}