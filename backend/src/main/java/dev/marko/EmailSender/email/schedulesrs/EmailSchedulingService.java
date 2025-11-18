package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.email.send.EmailSender;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import jakarta.mail.MessagingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSchedulingService {

    private final EmailSendService sendService;

    private final ScheduledExecutorService scheduler;

    @Getter
    @Value("${email.scheduling.default-delay-seconds}")
    private int defaultDelay;

    public void scheduleSingle(EmailMessage message, long delayInSeconds) {
        scheduler.schedule(() -> sendService.sendAndPersist(message),
                delayInSeconds, TimeUnit.SECONDS);
    }

    public void scheduleBatch(List<EmailMessage> messages, long intervalInSeconds) {
        for (int i = 0; i < messages.size(); i++) {
            EmailMessage message = messages.get(i);
            long delay = i * intervalInSeconds;

            scheduler.schedule(() -> sendService.sendAndPersist(message),
                    delay, TimeUnit.SECONDS);
        }
    }

    public void scheduleFollowUp(EmailMessage message, long delayInSeconds) {
        scheduler.schedule(() -> sendService.sendAndPersist(message),
                delayInSeconds, TimeUnit.SECONDS);
    }

}