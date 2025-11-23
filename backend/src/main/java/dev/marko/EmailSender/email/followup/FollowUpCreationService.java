package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class FollowUpCreationService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailPreparationService emailPreparationService;

    public EmailMessage createFollowUp(EmailMessage original, FollowUpTemplate template) {

        LocalDateTime scheduledTime = original.getSentAt().plusDays(template.getDelayDays());
        String processedMessage = emailPreparationService.generateMessageText(
                template.getMessage(),
                original.getRecipientName()
        );

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

        return emailMessageRepository.save(followUp);
    }

    public long calculateDelayInSeconds(LocalDateTime scheduledTime) {
        return Math.max(0, Duration.between(LocalDateTime.now(), scheduledTime).getSeconds());
    }
}