package dev.marko.EmailSender.email.followup;

import com.google.api.client.util.Value;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class FollowUpEligibilityService {

    @Value("${email.follow-up.quantity}")
    private int maximumFollowUps;

    private final EmailMessageRepository emailMessageRepository;

    public FollowUpTemplate findEligibleTemplate(EmailMessage original) {
        return original.getCampaign().getFollowUpTemplates().stream()
                .sorted(Comparator.comparingInt(FollowUpTemplate::getTemplateOrder))
                .limit(maximumFollowUps)
                .filter(t -> isReadyToSend(original, t))
                .findFirst()
                .orElse(null);
    }

    private boolean isReadyToSend(EmailMessage original, FollowUpTemplate template) {
        LocalDateTime scheduledTime = original.getSentAt().plusDays(template.getDelayDays());
        return !LocalDateTime.now().isBefore(scheduledTime)
                && !hasFollowUpAlreadyBeenSent(original, template);
    }

    public boolean hasFollowUpAlreadyBeenSent(EmailMessage original, FollowUpTemplate template) {
        return emailMessageRepository.existsByInReplyToAndFollowUpTemplate(
                original.getMessageId(),
                template
        );
    }

}
