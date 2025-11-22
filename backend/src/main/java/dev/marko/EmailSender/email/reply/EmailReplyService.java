package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.EmailReplyMapper;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EmailReplyService {

    private final CurrentUserProvider currentUserProvider;
    private final SmtpRepository smtpRepository;
    private final EmailReplyRepository replyRepository;
    private final EmailReplyMapper emailReplyMapper;

    private final ReplyResponseService replyResponseService;

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

    public EmailMessageDto respondToReply(Long replyId,
                                          EmailReplyResponseDto response){
        var user = currentUserProvider.getCurrentUser();

        EmailReply originalReply = replyRepository.findByIdAndUserId(replyId, user.getId()).orElseThrow();
        EmailMessage originalMessage = originalReply.getEmailMessage();

        var smtp = smtpRepository.findByIdAndUserId(originalMessage.getSmtpCredentials().getId(), user.getId())
                .orElseThrow(EmailNotFoundException::new);

        // return EmailMessageDto
        return replyResponseService.createReplyMessage(originalReply, originalMessage, response, smtp, user);

    }


    public void deleteReply(Long id){
        var user = currentUserProvider.getCurrentUser();

        var reply = replyRepository.findByIdAndUserId(id, user.getId()).orElseThrow();

        replyRepository.delete(reply);
    }

}
