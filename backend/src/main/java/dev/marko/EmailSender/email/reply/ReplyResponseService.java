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

/**
 * Service for creating and scheduling reply messages in response to email replies.
 * Handles reply message creation, persistence, and scheduling with error handling.
 */
@Slf4j
@AllArgsConstructor
@Service
public class ReplyResponseService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailMessageMapper emailMessageMapper;
    private final EmailSchedulingService emailSchedulingService;

    /**
     * Creates a reply message based on an original reply and schedules it for immediate sending.
     * If scheduling fails, marks the message as failed and throws an exception.
     *
     * @param originalReply the original email reply that triggered this response
     * @param originalMessage the original email message that was replied to
     * @param response the reply response data containing the message content
     * @param smtpCredentials the SMTP credentials to use for sending
     * @param user the user creating the reply
     * @return the created reply message DTO
     * @throws ReplyMessageSchedulingException if scheduling fails
     */
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
            emailSchedulingService.scheduleSingle(emailMessage, utcDateTime, 0);
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
