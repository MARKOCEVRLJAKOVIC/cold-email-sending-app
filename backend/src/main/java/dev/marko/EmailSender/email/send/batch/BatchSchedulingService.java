package dev.marko.EmailSender.email.send.batch;

import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.entities.EmailMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class BatchSchedulingService {

    private final EmailSchedulingService emailSchedulingService;

    public void scheduleEmails(LocalDateTime scheduledAt, List<EmailMessage> allMessages) {
        if (allMessages.isEmpty()) return;

        long defaultDelay = emailSchedulingService.getDefaultDelay();

        if (scheduledAt == null) {
            emailSchedulingService.scheduleBatch(allMessages, defaultDelay);
            return;
        }

        long baseDelay = Duration.between(LocalDateTime.now(), scheduledAt).getSeconds();
        for (int i = 0; i < allMessages.size(); i++) {
            long delay = baseDelay + i * defaultDelay;
            emailSchedulingService.scheduleSingle(allMessages.get(i), delay);
        }
    }
}
