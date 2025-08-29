package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.entities.EmailMessage;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailReplyDto {

    private Long id;
    private String originalMessageId;
    private String repliedMessageId;
    private String senderEmail;
    private LocalDateTime receivedAt;
    private String subject;
    private String content;
    private Long emailMessageId;
}
