package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Service for creating follow-up email messages based on original emails and templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpCreationService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailPreparationService emailPreparationService;

    /**
     * Creates a follow-up email message based on the original email and template.
     * The follow-up is scheduled based on the template's delay days from the original email's sent time.
     *
     * @param original the original email message
     * @param template the follow-up template to use
     * @return the created and persisted follow-up email message
     */
    @Transactional
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

        try {
            return emailMessageRepository.save(followUp);
        }
        catch (DataIntegrityViolationException e) {
            log.debug("Follow-up already sent for message {} with template {}",
                    original.getMessageId(), template.getId());
            return null;
        }
    }

    /**
     * Calculates the delay in seconds from the current time to the scheduled time.
     * Returns 0 if the scheduled time is in the past.
     *
     * @param scheduledTime the target scheduled time
     * @return the delay in seconds, or 0 if the scheduled time has passed
     */
    public long calculateDelayInSeconds(LocalDateTime scheduledTime) {
        return Math.max(0, Duration.between(LocalDateTime.now(ZoneId.of("UTC")), scheduledTime).getSeconds());
    }
}