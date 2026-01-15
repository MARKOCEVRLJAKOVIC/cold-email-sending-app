package dev.marko.EmailSender.email.schedulers;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
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
        log.error("Email failed for {}: {}", email.getRecipientEmail(), e.getMessage(), e);
    }
}