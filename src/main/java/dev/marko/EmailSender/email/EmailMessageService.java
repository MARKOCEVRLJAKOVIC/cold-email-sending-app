package dev.marko.EmailSender.email;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class EmailMessageService {

    private final AuthService authService;
    private final TemplateRepository templateRepository;
    private final SmtpRepository smtpRepository;
    private final CampaignRepository campaignRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailPreparationService preparationService;
    private final EmailCsvParser csvParser;
    private final EmailSchedulingService emailSchedulingService;


    public void sendBatchEmails(MultipartFile file,
                                LocalDateTime scheduledAt,
                                Long templateId,
                                List<Long> smtpIds,
                                Long campaignId) {

        var user = authService.getCurrentUser();

        var template = templateRepository.findById(templateId)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(TemplateNotFoundException::new);

        Campaign campaign = null;
        if (campaignId != null) {
            campaign = campaignRepository.findById(campaignId)
                    .filter(c -> c.getUser().getId().equals(user.getId()))
                    .orElse(null);
        }

        List<SmtpCredentials> smtpList = smtpRepository.findAllById(smtpIds).stream()
                .filter(s -> s.getUser().getId().equals(user.getId()))
                .toList();

        if (smtpList.isEmpty()) {
            throw new RuntimeException("No valid SMTP credentials selected");
        }

        List<String> failed = new ArrayList<>();
        List<EmailRecipientDto> recipients = csvParser.parse(file);
        List<EmailMessage> allMessages = new ArrayList<>();

        for (int i = 0; i < recipients.size(); i++) {
            EmailRecipientDto recipient = recipients.get(i);
            SmtpCredentials smtp = smtpList.get(i % smtpList.size()); // ✅ SMTP rotacija

            String messageText = preparationService.generateMessageText(template.getMessage(), recipient.getName());

            try {
                EmailMessage emailMessage = EmailMessageFactory.createMessageBasedOnSchedule(
                        recipient.getEmail(), recipient.getName(), messageText,
                        user, template, smtp, campaign, scheduledAt
                );

                allMessages.add(emailMessage);
                emailMessageRepository.save(emailMessage);


            } catch (Exception e) {
                failed.add(recipient.getEmail());

                var failedEmail = EmailMessageFactory.createFailedMessage(
                        recipient.getEmail(), recipient.getName(), messageText,
                        user, template, smtp, campaign, e.getMessage()
                );
                emailMessageRepository.save(failedEmail);
            }
        }

        scheduleEmails(scheduledAt, allMessages);
    }

    private void scheduleEmails(LocalDateTime scheduledAt, List<EmailMessage> allMessages) {
        if (allMessages.isEmpty()) return;

        if (scheduledAt == null) {
            emailSchedulingService.scheduleBatch(allMessages, 15);
            return;
        }

        long baseDelay = Duration.between(LocalDateTime.now(), scheduledAt).getSeconds();
        for (int i = 0; i < allMessages.size(); i++) {
            long delay = baseDelay + i * 15;
            emailSchedulingService.scheduleSingle(allMessages.get(i), delay);
        }
    }

}
