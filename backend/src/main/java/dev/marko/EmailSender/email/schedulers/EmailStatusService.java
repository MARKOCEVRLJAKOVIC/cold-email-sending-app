package dev.marko.EmailSender.email.schedulers;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.security.SensitiveDataMasker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Service for managing email message status updates.
 * Handles marking emails as sent, failed, or requeuing them with delays.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailStatusService {

    private final EmailMessageRepository repository;
    private final SensitiveDataMasker sensitiveDataMasker;

    /**
     * Marks an email message as sent and records the sent timestamp.
     *
     * @param emailId the ID of the email message to mark as sent
     */
    @Transactional
    public void markSent(Long emailId) {
        EmailMessage email = repository.findById(emailId).orElseThrow();
        email.setStatus(Status.SENT);
        email.setSentAt(LocalDateTime.now(ZoneId.of("UTC")));
        repository.save(email);
    }

    /**
     * Marks an email message as failed and logs the error.
     *
     * @param email the email message that failed
     * @param e the exception that caused the failure
     */
    @Transactional
    public void markFailed(EmailMessage email, Exception e) {
        email.setStatus(Status.FAILED);
        repository.save(email);
        String maskedEmail = sensitiveDataMasker.maskEmail(email.getRecipientEmail());
        log.error("Email failed for {}: {}", maskedEmail, e.getMessage(), e);
    }

    /**
     * Requeues an email message with a specified delay by updating its status to PENDING
     * and setting a new scheduled time.
     *
     * @param email the email message to requeue
     * @param delaySeconds the delay in seconds before the email should be processed again
     */
    @Transactional
    public void requeueWithDelay(EmailMessage email, long delaySeconds) {
        email.setStatus(Status.PENDING);
        email.setScheduledAt(LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(delaySeconds));
        repository.save(email);

        log.info("Email {} re-queued with delay of {}s", email.getId(), delaySeconds);
    }
}