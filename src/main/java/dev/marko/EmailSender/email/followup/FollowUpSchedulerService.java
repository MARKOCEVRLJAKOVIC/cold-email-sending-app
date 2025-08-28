package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.email.spintax.SpintaxProcessor;
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

    private final EmailMessageRepository emailMessageRepository;
    private final EmailSchedulingService schedulingService;
    private final SpintaxProcessor spintaxProcessor;

    @Value("${email.follow-up.quantity}")
    private int maximumFollowUps;

    @Value("${email.follow-up.expiration}")
    private Duration followUpExpiration;

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void sendFollowUps() {
        emailMessageRepository.findSentWithoutReplyOrFollowUp()
                .stream()
                .filter(this::isValidOriginalEmail)
                .forEach(this::processOriginalMessage);
    }

    private boolean isValidOriginalEmail(EmailMessage original) {
        return original.getSentAt() != null && original.getCampaign() != null;
    }

    private void processOriginalMessage(EmailMessage original) {
        getEligibleTemplates(original).stream()
                .filter(template -> shouldSendFollowUp(original, template))
                .findFirst()
                .ifPresent(template -> scheduleFollowUp(original, template));
    }

    private List<FollowUpTemplate> getEligibleTemplates(EmailMessage original) {
        return original.getCampaign().getFollowUpTemplates()
                .stream()
                .sorted(Comparator.comparingInt(FollowUpTemplate::getTemplateOrder))
                .limit(maximumFollowUps)
                .toList();
    }

    private boolean shouldSendFollowUp(EmailMessage original, FollowUpTemplate template) {
        LocalDateTime scheduledTime = original.getSentAt().plusDays(template.getDelayDays());
        return !LocalDateTime.now().isBefore(scheduledTime)
                && !emailMessageRepository.existsByInReplyToAndFollowUpTemplate(original.getMessageId(), template);
    }

    private void scheduleFollowUp(EmailMessage original, FollowUpTemplate template) {
        LocalDateTime scheduledTime = original.getSentAt().plusDays(template.getDelayDays());
        String processedMessage = renderTemplate(template.getMessage(), original.getRecipientName());

        EmailMessage followUp = EmailMessage.builder()
                .recipientEmail(original.getRecipientEmail())
                .recipientName(original.getRecipientName())
                .user(original.getUser())
                .campaign(original.getCampaign())
                .smtpCredentials(original.getSmtpCredentials())
                .status(Status.PENDING)
                .inReplyTo(original.getMessageId())
                .sentMessage(processedMessage)
                .followUpTemplate(template)
                .scheduledAt(scheduledTime)
                .build();

        emailMessageRepository.save(followUp);

        long delayInSeconds = Math.max(0, Duration.between(LocalDateTime.now(), scheduledTime).getSeconds());
        schedulingService.scheduleSingleFollowUp(followUp, delayInSeconds);

        log.info("Scheduled follow-up for {} at {} (delay: {}s)",
                followUp.getRecipientEmail(), scheduledTime, delayInSeconds);
    }

    private String renderTemplate(String message, String recipientName) {
        String result = spintaxProcessor.process(message);
        return result.replace("{{name}}", recipientName != null ? recipientName : "");
    }
}