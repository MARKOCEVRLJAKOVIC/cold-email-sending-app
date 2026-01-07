package dev.marko.EmailSender.email.send.batch;

import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@AllArgsConstructor
@Service
public class BatchSchedulingService {

    private final EmailSchedulingService emailSchedulingService;

    public void scheduleEmails(LocalDateTime scheduledAt, List<EmailMessage> allMessages, Campaign campaign) {
        if (allMessages.isEmpty()) return;

        long defaultDelay = emailSchedulingService.getDefaultDelay();

        if (scheduledAt == null) {
            emailSchedulingService.scheduleBatch(allMessages, defaultDelay);
            return;
        }

        ZoneId zone = ZoneId.of(campaign.getTimezone());

        long baseDelay = Duration.between(
                ZonedDateTime.now(zone),
                scheduledAt.atZone(zone)
        ).getSeconds();


        for (int i = 0; i < allMessages.size(); i++) {

            long delay = baseDelay + i * defaultDelay;
            emailSchedulingService.scheduleSingle(allMessages.get(i), delay, scheduledAt);

        }
    }
}