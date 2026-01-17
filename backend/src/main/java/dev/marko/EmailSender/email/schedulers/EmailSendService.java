package dev.marko.EmailSender.email.schedulers;

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
        EmailMessage lockedEmail = null;
        try {

            lockedEmail = lockService.lockForProcessing(email.getId());

            emailSenderDelegator.send(lockedEmail);
            statusService.markSent(lockedEmail.getId());

            return true;

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Email {} already processing, skipping.", email.getId());
            throw e;
        } catch (Exception e) {
            log.error("Error sending email {}: {}", email.getId(), e.getMessage(), e);
            // Use lockedEmail if available (has latest version), otherwise use original email
            if (lockedEmail != null) {
                statusService.markFailed(lockedEmail, e);
            } else {
                statusService.markFailed(email, e);
            }
            return false;
        }
    }
}
