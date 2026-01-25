package dev.marko.EmailSender.email.send.batch;

import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.send.EmailMessageFactory;
import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.security.SensitiveDataMasker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class EmailMessageCreationService {

    private final EmailPreparationService preparationService;
    private final EmailMessageRepository emailMessageRepository;
    private final SensitiveDataMasker sensitiveDataMasker;


    public List<EmailMessage> prepareAndSaveEmails(
            List<EmailRecipientDto> recipients,
            List<SmtpCredentials> smtpList,
            User user,
            EmailTemplate template,
            Campaign campaign,
            LocalDateTime scheduledAt
    ) {
        List<EmailMessage> messages = new ArrayList<>();
        ZoneId campaignZone = ZoneId.of(campaign.getTimezone());
        ZonedDateTime campaignTime = scheduledAt.atZone(campaignZone);
        LocalDateTime utcDateTime = LocalDateTime.ofInstant(campaignTime.toInstant(), ZoneId.of("UTC"));

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
                        user, template, smtp, campaign, utcDateTime
                );
                messages.add(emailMessage);

            } catch (Exception e) {
                String maskedEmail = sensitiveDataMasker.maskEmail(recipient.getEmail());
                log.error("Failed to create message for recipient {}: {}",
                        maskedEmail, e.getMessage());

                var failedEmail = EmailMessageFactory.createFailedMessage(
                        recipient.getEmail(), recipient.getName(), messageText,
                        user, template, smtp, campaign, e.getMessage()
                );
                messages.add(failedEmail);
            }
        }

        emailMessageRepository.saveAll(messages);
        return messages;

    }

}