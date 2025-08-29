package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SendBatchEmailsService {

    private final AuthService authService;
    private final TemplateRepository templateRepository;
    private final SmtpRepository smtpRepository;
    private final CampaignRepository campaignRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailPreparationService preparationService;
    private final EmailCsvParser csvParser;
    private final EmailSchedulingService emailSchedulingService;
    private final EmailMessageMapper emailMessageMapper;


    public List<EmailMessageDto> sendBatchEmails(MultipartFile file,
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

        Set< Long> foundIds = smtpList.stream()
                .map(SmtpCredentials::getId)
                .collect(Collectors.toSet());

        if (!foundIds.containsAll(smtpIds)) {
            throw new EmailNotFoundException();
        }

        if (smtpList.isEmpty()) {
            throw new RuntimeException("No valid SMTP credentials selected");
        }

        List<String> failed = new ArrayList<>();
        List<EmailRecipientDto> recipients = csvParser.parse(file);
        List<EmailMessage> allMessages = new ArrayList<>();

        for (int i = 0; i < recipients.size(); i++) {
            EmailRecipientDto recipient = recipients.get(i);
            SmtpCredentials smtp = smtpList.get(i % smtpList.size()); // smtp inbox rotation

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

        return allMessages.stream().map(emailMessageMapper::toDto)
                        .toList();

    }

    private void scheduleEmails(LocalDateTime scheduledAt, List<EmailMessage> allMessages) {
        if (allMessages.isEmpty()) return;

        long defaultDelay = emailSchedulingService.getDelayInSeconds();

        if (scheduledAt == null) {
            emailSchedulingService.scheduleBatch(allMessages, defaultDelay);
            return;
        }

        long baseDelay = Duration.between(LocalDateTime.now(), scheduledAt).getSeconds();
        for (int i = 0; i < allMessages.size(); i++) {
            long delay = baseDelay + i * defaultDelay;
            emailSchedulingService.scheduleSingle(allMessages.get(i), delay);
        }
    }

}
