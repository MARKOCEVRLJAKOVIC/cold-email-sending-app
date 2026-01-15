package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.email.schedulers.EmailSchedulingService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpSchedulerService {

    private final FollowUpQueryService queryService;
    private final FollowUpEligibilityService eligibilityService;
    private final FollowUpCreationService creationService;
    private final EmailSchedulingService schedulingService;

    @Scheduled(cron = "0 0 * * * *")
    public void scheduleFollowUps() {
        queryService.getEligibleOriginalEmails()
                .forEach(this::processOriginalEmail);
    }

    private void processOriginalEmail(EmailMessage original) {
        FollowUpTemplate template = eligibilityService.findEligibleTemplate(original);

        if (template == null) return;

        EmailMessage followUp = creationService.createFollowUp(original, template);

        long delay = creationService.calculateDelayInSeconds(followUp.getScheduledAt());
        schedulingService.scheduleFollowUp(followUp, delay);

        log.info("Scheduled follow-up for {} at {} (delay {}s)",
                followUp.getRecipientEmail(),
                followUp.getScheduledAt(),
                delay
        );
    }
}