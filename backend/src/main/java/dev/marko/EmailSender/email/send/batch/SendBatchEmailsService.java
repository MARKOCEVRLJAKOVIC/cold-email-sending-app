package dev.marko.EmailSender.email.send.batch;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.dtos.EmailRecipientDto;
import dev.marko.EmailSender.email.connection.gmailOAuth.SmtpListIsEmptyException;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.exception.RateLimitExceededException;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.ratelimit.UserRateLimiter;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SendBatchEmailsService {

    private final CurrentUserProvider currentUserProvider;
    private final TemplateRepository templateRepository;
    private final SmtpRepository smtpRepository;
    private final CampaignRepository campaignRepository;
    private final EmailMessageMapper emailMessageMapper;
    private final CsvParserService csvParserService;
    private final EmailMessageCreationService emailMessageCreationService;
    private final BatchSchedulingService batchSchedulingService;
    private final UserRateLimiter userRateLimiter;

    public List<EmailMessageDto> sendBatchEmails(MultipartFile file,
                                                 LocalDateTime scheduledAt,
                                                 Long templateId,
                                                 List<Long> smtpIds,
                                                 Long campaignId) {

        var user = currentUserProvider.getCurrentUser();

        userRateLimiter.consumeBatchOrThrow(user);

        var template = getTemplateFromUser(templateId, user);
        var campaign = findCampaignFromUser(campaignId, user.getId());

        List<SmtpCredentials> smtpList = validateAndGetSmptList(smtpIds, user.getId());
        List<EmailRecipientDto> recipients = csvParserService.parseCsv(file);

        userRateLimiter.consumeEmailsOrThrow(user, recipients.size());

        List<EmailMessage> allMessages = emailMessageCreationService.prepareAndSaveEmails(
                recipients, smtpList, user,
                template, campaign, scheduledAt
        );

        batchSchedulingService.scheduleEmails(scheduledAt, allMessages, campaign);

        return allMessages.stream().map(emailMessageMapper::toDto)
                        .toList();

    }

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
        return templateRepository.findByIdAndUserId(templateId, user.getId())
                .orElseThrow(TemplateNotFoundException::new);
    }

    private Campaign findCampaignFromUser(Long campaignId, Long userId) {
        return campaignRepository.findByIdAndUserId(campaignId, userId)
                .orElseThrow(CampaignNotFoundException::new);
    }



}
