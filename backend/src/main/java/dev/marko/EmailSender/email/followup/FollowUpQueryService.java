package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpQueryService {

    private final EmailMessageRepository emailMessageRepository;

    public List<EmailMessage> getEligibleOriginalEmails() {
        return emailMessageRepository.findSentWithoutReplyOrFollowUp()
                .stream()
                .filter(email -> email.getSentAt() != null && email.getCampaign() != null)
                .toList();
    }
}
