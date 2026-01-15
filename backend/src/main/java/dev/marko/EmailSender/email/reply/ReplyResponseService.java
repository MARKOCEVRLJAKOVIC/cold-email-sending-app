package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.email.schedulers.EmailSchedulingService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.exception.ReplyMessageSchedulingException;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@AllArgsConstructor
@Service
public class ReplyResponseService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailMessageMapper emailMessageMapper;
    private final EmailSchedulingService emailSchedulingService;

    @Transactional
    public EmailMessageDto createReplyMessage(EmailReply originalReply,
                                               EmailMessage originalMessage,
                                               EmailReplyResponseDto response,
                                               SmtpCredentials smtpCredentials,
                                               User user){

        LocalDateTime utcDateTime = LocalDateTime.now((ZoneId.of("UTC")));

        var emailMessage = EmailMessage.builder()
                .recipientEmail(originalReply.getSenderEmail())
                .recipientName(originalMessage.getRecipientName())
                .scheduledAt(utcDateTime)
                .emailTemplate(originalMessage.getEmailTemplate())
                .campaign(originalMessage.getCampaign())
                .smtpCredentials(smtpCredentials)
                .status(Status.PENDING)
                .sentMessage(response.getMessage())
                .user(user)
                .inReplyTo(originalReply.getRepliedMessageId())
                .build();

        try {
            emailMessageRepository.save(emailMessage);
            emailSchedulingService.scheduleSingle(emailMessage, 0, utcDateTime);
        }
        catch (Exception e) {

            emailMessage.setStatus(Status.FAILED);
            emailMessage.setErrorMessage(e.getMessage());
            emailMessageRepository.save(emailMessage);

            throw new ReplyMessageSchedulingException();

        }


        return emailMessageMapper.toDto(emailMessage);

    }
}
