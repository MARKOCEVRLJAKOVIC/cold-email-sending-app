package dev.marko.EmailSender.dtos;

import lombok.Data;

@Data
public class EmailRequest {

    private Long templateId;
    private Long smtpId;
    private String recipientEmail;

}
