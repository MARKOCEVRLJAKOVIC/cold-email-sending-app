package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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