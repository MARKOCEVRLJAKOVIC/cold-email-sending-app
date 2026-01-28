package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;

/**
 * Service for determining eligibility of follow-up templates for original emails.
 * Checks if an original email can receive a follow-up based on reply status and template readiness.
 */
@Service
@RequiredArgsConstructor
public class FollowUpEligibilityService {

    @Value("${email.follow-up.quantity}")
    private int maximumFollowUps;

    private final EmailMessageRepository emailMessageRepository;
    private final EmailReplyRepository emailReplyRepository;

    /**
     * Finds the first eligible follow-up template for the given original email.
     * Returns null if the original email has been replied to or no eligible template is found.
     *
     * @param original the original email message
     * @return the first eligible follow-up template, or null if none found
     */
    public FollowUpTemplate findEligibleTemplate(EmailMessage original) {

        // check if the original message is replied to, if yes then cancel followup
        if (emailReplyRepository.existsByEmailMessageId(original.getId())) {
            return null;
        }

        return original.getCampaign().getFollowUpTemplates().stream()
                .sorted(Comparator.comparingInt(FollowUpTemplate::getTemplateOrder))
                .limit(maximumFollowUps)
                .filter(t -> isReadyToSend(original, t))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if a follow-up template is ready to be sent for the given original email.
     * A template is ready if the delay period has passed and the follow-up hasn't been sent already.
     *
     * @param original the original email message
     * @param template the follow-up template to check
     * @return true if the template is ready to send, false otherwise
     */
    private boolean isReadyToSend(EmailMessage original, FollowUpTemplate template) {
        LocalDateTime eligibleFromTime = original.getSentAt().plusDays(template.getDelayDays());
        return LocalDateTime.now(ZoneId.of("UTC")).isAfter(eligibleFromTime)
                && !followUpAlreadySent(original, template);
    }

    /**
     * Checks if a follow-up email using the given template has already been sent for the original email.
     *
     * @param original the original email message
     * @param template the follow-up template to check
     * @return true if the follow-up has already been sent, false otherwise
     */
    private boolean followUpAlreadySent(EmailMessage original, FollowUpTemplate template) {
        return emailMessageRepository.existsByInReplyToAndFollowUpTemplateId(
                original.getMessageId(),
                template.getId()
        );
    }

}
