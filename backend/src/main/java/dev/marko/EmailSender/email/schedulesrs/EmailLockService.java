package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class EmailLockService {
    private final EmailMessageRepository repository;

    @Transactional(propagation = REQUIRES_NEW)
    public void lockForProcessing(Long emailId) {
        EmailMessage email = repository.findById(emailId)
                .orElseThrow(() -> new ObjectOptimisticLockingFailureException(EmailMessage.class, emailId));
        if(email.getStatus() != Status.PENDING){
            throw new ObjectOptimisticLockingFailureException(
                    EmailMessage.class, email.getId());
        }

        email.setStatus(Status.PROCESSING);
        repository.saveAndFlush(email);
    }
}