package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for querying original emails that are eligible for follow-up processing.
 */
@Service
@RequiredArgsConstructor
public class FollowUpQueryService {

    private final EmailMessageRepository emailMessageRepository;

    /**
     * Retrieves all original emails that are eligible for follow-up processing.
     * Filters emails that have been sent, have no replies, no existing follow-ups, and belong to a campaign.
     *
     * @return list of eligible original email messages
     */
    public List<EmailMessage> getEligibleOriginalEmails() {
        return emailMessageRepository.findSentWithoutReplyOrFollowUp()
                .stream()
                .filter(email -> email.getSentAt() != null && email.getCampaign() != null)
                .toList();
    }
}
