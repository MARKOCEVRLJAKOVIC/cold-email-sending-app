package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.connection.gmailOAuth.SmtpListIsEmptyException;
import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SendBatchEmailsService {

    private final AuthService authService;
    private final TemplateRepository templateRepository;
    private final SmtpRepository smtpRepository;
    private final CampaignRepository campaignRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailPreparationService preparationService;
    private final EmailSchedulingService emailSchedulingService;
    private final EmailMessageMapper emailMessageMapper;


    public List<EmailMessageDto> sendBatchEmails(MultipartFile file,
                                                 LocalDateTime scheduledAt,
                                                 Long templateId,
                                                 List<Long> smtpIds,
                                                 Long campaignId) {

        var user = authService.getCurrentUser();
        var template = getTemplateFromUser(templateId, user);
        var campaign = findCampaignFromUser(campaignId, user.getId());

        List<SmtpCredentials> smtpList = validateAndGetSmptList(smtpIds, user.getId());
        List<EmailRecipientDto> recipients = parseCsv(file);
        List<EmailMessage> allMessages = prepareAndSaveEmails(
                recipients, smtpList, user,
                template, campaign, scheduledAt
        );


        scheduleEmails(scheduledAt, allMessages);

        return allMessages.stream().map(emailMessageMapper::toDto)
                        .toList();

    }

    @NotNull
    private List<SmtpCredentials> validateAndGetSmptList(List<Long> smtpIds, Long userId) {
        if (smtpIds == null || smtpIds.isEmpty()) {
            throw new SmtpListIsEmptyException();
        }

        var smtpList = smtpRepository.findAllById(smtpIds).stream()
                .filter(s -> s.getUser().getId().equals(userId))
                .toList();

        if (smtpList.isEmpty()) {
            throw new SmtpListIsEmptyException();
        }

        return smtpList;
    }

    private EmailTemplate getTemplateFromUser(Long templateId, User user) {
        return templateRepository.findById(templateId)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(TemplateNotFoundException::new);
    }

    private Campaign findCampaignFromUser(Long campaignId, Long userId) {
        if (campaignId == null) return null;

        return campaignRepository.findById(campaignId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElse(null);
    }

    private List<EmailMessage> prepareAndSaveEmails(
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

    private List<EmailRecipientDto> parseCsv(MultipartFile file)  {
        List<EmailRecipientDto> recipients = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirst = true;

            while ((line = reader.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    continue;
                }

                String[] tokens = line.split(",");


                String email = tokens[0].trim();
                String name = tokens.length > 1 ? tokens[1].trim() : "";

                if (email.isEmpty()) continue;

                var dto = new EmailRecipientDto(name, email);
                recipients.add(dto);

            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }

        return recipients;

    }

}
