package dev.marko.EmailSender.email.reply;

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
