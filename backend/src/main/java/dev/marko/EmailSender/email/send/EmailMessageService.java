package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class EmailMessageService {

    private final SendBatchEmailsService sendBatchEmailsService;
    private final EmailMessageRepository emailMessageRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EmailMessageMapper emailMessageMapper;

    public List<EmailMessageDto> findAllMessagesFromUser(){
        var user = currentUserProvider.getCurrentUser();
        var emailMessageList = emailMessageRepository
                .findAllByUserIdAndStatusIn(user.getId(), List.of(Status.SENT, Status.REPLIED));

        return emailMessageMapper.toListDto(emailMessageList);
    }

    public List<EmailMessageDto> findAllMessagesFromCampaign(Long campaignId){
        var user = currentUserProvider.getCurrentUser();

        var emailMessageList = emailMessageRepository
                .findAllByCampaignIdAndUserIdAndStatusIn(campaignId, user.getId(), List.of(Status.SENT, Status.REPLIED));
        

        return emailMessageMapper.toListDto(emailMessageList);
    }

    public EmailMessageDto getEmailMessage(Long id){
        var user = currentUserProvider.getCurrentUser();

        var emailMessage = emailMessageRepository.findByIdAndUserId(id, user.getId()).orElseThrow(EmailMessageNotFoundException::new);
        return emailMessageMapper.toDto(emailMessage);

    }

    public List<EmailMessageDto> sendBatchEmails(MultipartFile file,
                                                 LocalDateTime scheduledAt,
                                                 Long templateId,
                                                 List<Long> smtpIds,
                                                 Long campaignId){

        return sendBatchEmailsService.sendBatchEmails(file, scheduledAt, templateId, smtpIds, campaignId);

    }

    public EmailMessageDto updateEmailMessage(Long id, UpdateEmailMessageRequest request){

        var user = currentUserProvider.getCurrentUser();

        var emailMessage = emailMessageRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(EmailMessageNotFoundException::new);

        if (emailMessage.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Email can no longer be edited.");
        }

        emailMessageMapper.update(request, emailMessage);

        emailMessageRepository.save(emailMessage);

        return emailMessageMapper.toDto(emailMessage);
    }

    public void deleteEmailMessage(Long id){
        var user = currentUserProvider.getCurrentUser();

        var emailMessage = emailMessageRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(EmailMessageNotFoundException::new);

        emailMessageRepository.delete(emailMessage);

    }

}
