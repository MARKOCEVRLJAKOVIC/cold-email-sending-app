package dev.marko.EmailSender.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendEmailRequest {
    @NotBlank
    private String recipientEmail;
    @NotBlank
    private String recipientName;

    @NotNull
    private Long templateId;
    @NotNull
    private Long smtpId;
    private Long campaignId;
}
