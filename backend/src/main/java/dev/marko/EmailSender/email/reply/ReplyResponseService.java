package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.email.schedulesrs.EmailSchedulingService;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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


        var emailMessage = EmailMessage.builder()
                .recipientEmail(originalReply.getSenderEmail())
                .recipientName(originalMessage.getRecipientName())
                .scheduledAt(LocalDateTime.now())
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
            emailSchedulingService.scheduleSingle(emailMessage, 0);
        }
        catch (Exception e) {

            emailMessage.setStatus(Status.FAILED);
            emailMessageRepository.save(emailMessage);

            throw new RuntimeException("Failed to schedule reply message", e);

        }


        return emailMessageMapper.toDto(emailMessage);

    }
}
