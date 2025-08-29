package dev.marko.EmailSender.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class EmailMessageRequest {

    private String recipientEmail;
    private String recipientName;
    private String messageText;
    private LocalDateTime scheduledAt;

    private Long userId;
    private Long templateId;
    private Long smtpId;
    private Long campaignId;
}
