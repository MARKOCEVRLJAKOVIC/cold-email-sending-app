package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.EmailReply;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.mappers.EmailReplyMapper;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class EmailReplyService {

    private final CurrentUserProvider currentUserProvider;
    private final SmtpRepository smtpRepository;
    private final EmailSchedulingService schedulingService;
    private final EmailReplyRepository replyRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailMessageMapper emailMessageMapper;
    private final EmailReplyMapper emailReplyMapper;


    public List<EmailReplyDto> getAllRepliesFromUser(){
        var user = currentUserProvider.getCurrentUser();

        var emailRepliesList = replyRepository.findAllByUserId(user.getId());

        return emailReplyMapper.toListDto(emailRepliesList);
    }

    public EmailReplyDto getEmailReply(Long id){

        var user = currentUserProvider.getCurrentUser();

        var emailReply = replyRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(EmailReplyNotFoundException::new);

        return emailReplyMapper.toDto(emailReply);

    }

    public EmailMessageDto replyToReply(Long replyId,
                                        EmailReplyResponseDto response){
        var user = currentUserProvider.getCurrentUser();

        EmailReply originalReply = replyRepository.findByIdAndUserId(replyId, user.getId()).orElseThrow();
        EmailMessage originalMessage = originalReply.getEmailMessage();

        var smtp = smtpRepository.findByIdAndUserId(originalMessage.getSmtpCredentials().getId(), user.getId())
                .orElseThrow(EmailNotFoundException::new);

        var emailMessage = EmailMessage.builder()
                .recipientEmail(originalReply.getSenderEmail())
                .recipientName(originalMessage.getRecipientName())
                .scheduledAt(LocalDateTime.now())
                .emailTemplate(originalMessage.getEmailTemplate())
                .campaign(originalMessage.getCampaign())
                .smtpCredentials(smtp)
                .status(Status.PENDING)
                .sentMessage(response.getMessage())
                .user(user)
                .inReplyTo(originalReply.getRepliedMessageId())
                .build();

        emailMessageRepository.save(emailMessage);
        var emailMessageDto = emailMessageMapper.toDto(emailMessage);

        schedulingService.scheduleSingle(emailMessage, 0);

        return emailMessageDto;
    }

    public void deleteReply(Long id){
        var user = currentUserProvider.getCurrentUser();

        var reply = replyRepository.findByIdAndUserId(id, user.getId()).orElseThrow();

        replyRepository.delete(reply);
    }

}
