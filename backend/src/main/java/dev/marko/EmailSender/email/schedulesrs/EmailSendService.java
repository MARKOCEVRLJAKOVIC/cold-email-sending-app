package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.email.send.EmailSenderDelegator;
import dev.marko.EmailSender.entities.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendService {

    private final EmailLockService lockService;
    private final EmailSenderDelegator emailSenderDelegator;
    private final EmailStatusService statusService;

    public boolean sendAndPersist(EmailMessage email) {
        try {

            EmailMessage lockedEmail = lockService.lockForProcessing(email.getId());

            emailSenderDelegator.send(lockedEmail);
            statusService.markSent(lockedEmail.getId());

            return true;

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Email {} already processing, skipping.", email.getId());
            throw e;
        } catch (Exception e) {
            statusService.markFailed(email, e);
            return false;
        }
    }
}
