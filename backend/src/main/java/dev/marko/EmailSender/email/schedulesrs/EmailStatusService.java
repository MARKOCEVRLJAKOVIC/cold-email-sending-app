package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
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

    public void markSent(EmailMessage email) {
        email.setStatus(Status.SENT);
        email.setSentAt(LocalDateTime.now(ZoneId.of("UTC")));
        repository.save(email);
    }

    public void markFailed(EmailMessage email, Exception e) {
        email.setStatus(Status.FAILED);
        repository.save(email);
        log.error("Email failed for {}: {}", email.getRecipientEmail(), e.getMessage(), e);
    }
}