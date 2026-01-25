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

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailStatusService {

    private final EmailMessageRepository repository;
    private final SensitiveDataMasker sensitiveDataMasker;

    @Transactional
    public void markSent(Long emailId) {
        EmailMessage email = repository.findById(emailId).orElseThrow();
        email.setStatus(Status.SENT);
        email.setSentAt(LocalDateTime.now(ZoneId.of("UTC")));
        repository.save(email);
    }

    @Transactional
    public void markFailed(EmailMessage email, Exception e) {
        email.setStatus(Status.FAILED);
        repository.save(email);
        String maskedEmail = sensitiveDataMasker.maskEmail(email.getRecipientEmail());
        log.error("Email failed for {}: {}", maskedEmail, e.getMessage(), e);
    }
}