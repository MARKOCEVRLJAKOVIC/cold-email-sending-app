package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.email.schedulers.EmailSchedulingService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.security.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service responsible for scheduling follow-up emails based on scheduled cron job.
 * Processes eligible original emails and creates follow-up messages when appropriate templates are available.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpSchedulerService {

    private final FollowUpQueryService queryService;
    private final FollowUpEligibilityService eligibilityService;
    private final FollowUpCreationService creationService;
    private final EmailSchedulingService schedulingService;
    private final SensitiveDataMasker sensitiveDataMasker;

    /**
     * Scheduled task that runs every hour to process and schedule follow-up emails.
     * Retrieves eligible original emails and processes each one to create and schedule follow-ups.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleFollowUps() {
        queryService.getEligibleOriginalEmails()
                .forEach(this::processOriginalEmail);
    }

    /**
     * Processes a single original email to create and schedule a follow-up if eligible.
     *
     * @param original the original email message to process
     */
    private void processOriginalEmail(EmailMessage original) {
        FollowUpTemplate template = eligibilityService.findEligibleTemplate(original);

        if (template == null) return;

        EmailMessage followUp = creationService.createFollowUp(original, template);

        long delay = creationService.calculateDelayInSeconds(followUp.getScheduledAt());
        schedulingService.scheduleFollowUp(followUp, delay);

        String maskedEmail = sensitiveDataMasker.maskEmail(followUp.getRecipientEmail());

        log.info("Scheduled follow-up for {} at {} (delay {}s)",
                maskedEmail,
                followUp.getScheduledAt(),
                delay
        );
    }
}