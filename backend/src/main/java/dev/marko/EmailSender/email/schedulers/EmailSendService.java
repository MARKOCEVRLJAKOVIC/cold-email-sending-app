package dev.marko.EmailSender.email.schedulers;

import dev.marko.EmailSender.email.send.EmailSenderDelegator;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.ratelimit.SmtpRateLimiter;
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
    private final SmtpRateLimiter smtpRateLimiter;

    public boolean sendAndPersist(EmailMessage email) {
        try {

            EmailMessage lockedEmail = lockService.lockForProcessing(email.getId());

            if (!smtpRateLimiter.canSendEmail(lockedEmail.getSmtpCredentials())) {
                log.warn("Rate limit exceeded for SMTP {}, re-queueing email {}",
                        lockedEmail.getSmtpCredentials().getEmail(),
                        lockedEmail.getId());

                // Mark as pending again and requeue with delay
                lockedEmail.setStatus(Status.PENDING);
                statusService.requeueWithDelay(lockedEmail,
                        smtpRateLimiter.getMinimumDelay(lockedEmail.getSmtpCredentials()));

                return false;
            }

            emailSenderDelegator.send(lockedEmail);
            statusService.markSent(lockedEmail.getId());

            return true;

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Email {} already processing, skipping.", email.getId());
            throw e;
        } catch (Exception e) {
            log.error("Failed to send email {}: ", email.getId(), e);
            statusService.markFailed(email, e);
            return false;
        }
    }
}
