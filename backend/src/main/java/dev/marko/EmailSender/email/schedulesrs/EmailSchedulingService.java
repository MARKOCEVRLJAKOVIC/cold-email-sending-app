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

    private final EmailSender emailSender;
    private final EmailMessageRepository emailMessageRepository;

    @Getter
    @Value("${email.scheduling.default-delay-seconds}")
    private int delayInSeconds;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public void scheduleBatch(List<EmailMessage> messages, long intervalInSeconds) {
        for (int i = 0; i < messages.size(); i++) {
            EmailMessage message = messages.get(i);
            long delay = i * intervalInSeconds;

            scheduler.schedule(() -> {
                try {
                    sendAndPersist(message);
                } catch (Exception e) {
                    log.error("Unexpected error while sending email to {}: {}",
                            message.getRecipientEmail(), e.getMessage(), e);
                    message.setStatus(Status.FAILED);
                    emailMessageRepository.save(message);
                }
            }, delay, TimeUnit.SECONDS);
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
        } catch (Exception e) {
            email.setStatus(Status.FAILED);
            log.error("Unexpected error sending email to {}: {}", email.getRecipientEmail(), e.getMessage(), e);
        }
        emailMessageRepository.save(email);
    }
    public void scheduleSingle(EmailMessage message, long delayInSeconds) {
        scheduler.schedule(() -> {
            try {
                sendAndPersist(message);
            } catch (Exception e) {
                log.error("Unexpected error while sending single email to {}: {}",
                        message.getRecipientEmail(), e.getMessage(), e);
                message.setStatus(Status.FAILED);
                emailMessageRepository.save(message);
            }
        }, delayInSeconds, TimeUnit.SECONDS);
    }

    public void sendAndPersistFollowUp(EmailMessage email) {
        try {
            emailSender.sendEmails(email);
            email.setStatus(Status.SENT);
            email.setSentAt(LocalDateTime.now());
        } catch (MessagingException e) {
            email.setStatus(Status.FAILED);
            log.error("Failed to send follow-up email to {}: {}", email.getRecipientEmail(), e.getMessage());
        } catch (Exception e) {
            email.setStatus(Status.FAILED);
            log.error("Unexpected error sending follow-up email to {}: {}", email.getRecipientEmail(), e.getMessage(), e);
        }
        emailMessageRepository.save(email);
    }

    public void scheduleSingleFollowUp(EmailMessage message, long delayInSeconds) {
        scheduler.schedule(() -> {
            try {
                sendAndPersistFollowUp(message);
            } catch (Exception e) {
                log.error("Unexpected error while sending follow-up email to {}: {}",
                        message.getRecipientEmail(), e.getMessage(), e);
                message.setStatus(Status.FAILED);
                emailMessageRepository.save(message);
            }
        }, delayInSeconds, TimeUnit.SECONDS);
    }

}