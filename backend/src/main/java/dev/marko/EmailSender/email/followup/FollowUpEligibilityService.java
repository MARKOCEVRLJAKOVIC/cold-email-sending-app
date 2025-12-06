package dev.marko.EmailSender.email.followup;

import com.google.api.client.util.Value;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class FollowUpEligibilityService {

    @Value("${email.follow-up.quantity}")
    private int maximumFollowUps;

    private final EmailMessageRepository emailMessageRepository;
    private final EmailReplyRepository emailReplyRepository;

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

    private boolean isReadyToSend(EmailMessage original, FollowUpTemplate template) {
        LocalDateTime eligibleFromTime = original.getSentAt().plusDays(template.getDelayDays());
        return LocalDateTime.now(ZoneId.of("UTC")).isAfter(eligibleFromTime)
                && !followUpAlreadySent(original, template);
    }

    private boolean followUpAlreadySent(EmailMessage original, FollowUpTemplate template) {
        return emailMessageRepository.existsByInReplyToAndFollowUpTemplateId(
                original.getMessageId(),
                template.getId()
        );
    }

}
