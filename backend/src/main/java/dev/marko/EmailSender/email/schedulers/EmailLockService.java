package dev.marko.EmailSender.email.schedulers;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Service for locking email messages during processing to prevent concurrent processing.
 * Uses optimistic locking to ensure only one process can handle an email at a time.
 */
@Service
@RequiredArgsConstructor
public class EmailLockService {
    private final EmailMessageRepository repository;

    /**
     * Locks an email message for processing by changing its status to PROCESSING.
     * Throws an exception if the email is not in PENDING status or cannot be found.
     *
     * @param emailId the ID of the email message to lock
     * @return the locked email message with PROCESSING status
     * @throws ObjectOptimisticLockingFailureException if the email is not in PENDING status or not found
     */
    @Transactional(propagation = REQUIRES_NEW)
    public EmailMessage lockForProcessing(Long emailId) {

        EmailMessage email = repository.findByIdWithDetails(emailId)
                .orElseThrow(() -> new ObjectOptimisticLockingFailureException(EmailMessage.class, emailId));

        if(email.getStatus() != Status.PENDING){
            throw new ObjectOptimisticLockingFailureException(
                    EmailMessage.class, email.getId());
        }

        email.setStatus(Status.PROCESSING);
        return repository.saveAndFlush(email);

    }
}