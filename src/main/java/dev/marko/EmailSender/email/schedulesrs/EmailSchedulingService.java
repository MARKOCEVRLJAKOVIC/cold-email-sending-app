package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.email.EmailSender;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final EmailSender emailSender;
    private final EmailMessageRepository emailMessageRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public void scheduleBatch(List<EmailMessage> messages, long intervalInSeconds) {
        for (int i = 0; i < messages.size(); i++) {
            EmailMessage message = messages.get(i);
            long delay = i * intervalInSeconds;

            scheduler.schedule(() -> sendAndPersist(message), delay, TimeUnit.SECONDS);
        }
    }
    private void sendAndPersist(EmailMessage email) {
        try {
            emailSender.sendEmails(email);
            email.setStatus(Status.SENT);
            email.setSentAt(LocalDateTime.now());
            log.info("Email sent to {}", email.getRecipientEmail());
        } catch (MessagingException e) {
            email.setStatus(Status.FAILED);
            log.error("Failed to send email to {}: {}", email.getRecipientEmail(), e.getMessage());
        }

        emailMessageRepository.save(email);
    }
    public void scheduleSingle(EmailMessage email, long delayInSeconds) {
        scheduler.schedule(() -> sendAndPersist(email), delayInSeconds, TimeUnit.SECONDS);
    }
}