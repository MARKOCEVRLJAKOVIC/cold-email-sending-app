package dev.marko.EmailSender.email.send.batch;

import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.send.EmailMessageFactory;
import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class EmailMessageCreationService {

    private final EmailPreparationService preparationService;
    private final EmailMessageRepository emailMessageRepository;


    public List<EmailMessage> prepareAndSaveEmails(
            List<EmailRecipientDto> recipients,
            List<SmtpCredentials> smtpList,
            User user,
            EmailTemplate template,
            Campaign campaign,
            LocalDateTime scheduledAt
    ) {
        List<EmailMessage> messages = new ArrayList<>();

        for (int i = 0; i < recipients.size(); i++) {
            var recipient = recipients.get(i);
            var smtp = smtpList.get(i % smtpList.size()); // rotate inbox
            var messageText = preparationService.generateMessageText(
                    template.getMessage(), recipient.getName()
            );

            try {
                var emailMessage = EmailMessageFactory.createMessageBasedOnSchedule(
                        recipient.getEmail(),
                        recipient.getName(),
                        messageText,
                        user, template, smtp, campaign, scheduledAt
                );
                emailMessageRepository.save(emailMessage);
                messages.add(emailMessage);

            } catch (Exception e) {
                log.error("Failed to create message for recipient {}: {}",
                        recipient.getEmail(), e.getMessage());

                var failedEmail = EmailMessageFactory.createFailedMessage(
                        recipient.getEmail(), recipient.getName(), messageText,
                        user, template, smtp, campaign, e.getMessage()
                );
                emailMessageRepository.save(failedEmail);
            }
        }

        return messages;
    }

}
